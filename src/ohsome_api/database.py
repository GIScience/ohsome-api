import asyncio
import json
import logging
from contextlib import asynccontextmanager
from typing import Any, AsyncIterator

import asyncpg
from asyncpg import Connection, Record

from ohsome_api.config import CONFIG

CONNECTION_STRING = CONFIG.ohsomedb.connection_string
SCHEMA = CONFIG.ohsomedb.schemaname

logger = logging.getLogger("ohsome-api")


class PoolAcquireTimeoutError(TimeoutError):
    pass


class QueryTimeoutError(TimeoutError):
    def __init__(self) -> None:
        message = (
            f"Query timeout limit has been exceeded. "
            f"For statistics endpoints the timeout limit is "
            f"{CONFIG.ohsomedb.timeout_stats}. "
            f"For extraction endpoints the timeout limit is "
            f"{CONFIG.ohsomedb.timeout_extraction}."
        )
        super().__init__(message)


async def jsonb_codec(connection: Connection) -> None:
    await connection.set_type_codec(
        "jsonb",
        encoder=(lambda x: x),
        decoder=json.loads,
        schema="pg_catalog",
    )


class Database:
    def __init__(self) -> None:
        self.pool: asyncpg.Pool | None = None
        self.pool_extraction: asyncpg.Pool | None = None

    async def connect(self) -> None:
        # Initialize the pools once
        self.pool = await asyncpg.create_pool(
            dsn=CONNECTION_STRING,
            min_size=CONFIG.ohsomedb.pool_min_size_stats,
            max_size=CONFIG.ohsomedb.pool_max_size_stats,
            init=jsonb_codec,
            command_timeout=CONFIG.ohsomedb.timeout_stats,  # query timeout
            server_settings={
                "application_name": "ohsome-api",
                "search_path": f"{SCHEMA},public",
            },
        )
        self.pool_extraction = await asyncpg.create_pool(
            dsn=CONNECTION_STRING,
            min_size=CONFIG.ohsomedb.pool_min_size_extraction,
            max_size=CONFIG.ohsomedb.pool_max_size_extraction,
            init=jsonb_codec,
            command_timeout=CONFIG.ohsomedb.timeout_extraction,  # query timeout
            server_settings={
                "application_name": "ohsome-api",
                "search_path": f"{SCHEMA},public",
            },
        )
        logging.info("Database connection pools established.")

    async def disconnect(self) -> None:
        if self.pool:
            await self.pool.close()

        if self.pool_extraction:
            await self.pool_extraction.close()
        logging.info("Database connection pools closed.")

    @asynccontextmanager
    async def acquire_connection(self, timeout: int = 10) -> AsyncIterator[Connection]:
        if self.pool is None:
            raise ValueError("Database connection pool not initialized")

        acquiring = True
        try:
            async with self.pool.acquire(timeout=timeout) as connection:
                acquiring = False
                yield connection
        except TimeoutError as error:
            # Only raise custom error TimeoutError is thrown during acquiring
            if acquiring:
                raise PoolAcquireTimeoutError(
                    f"Could not acquire connection within {timeout}s"
                ) from error
            raise

    async def fetch_row(self, sql: str, *args: Any) -> Record:  # noqa: ANN401
        async with self.acquire_connection() as connection:
            try:
                record: Record = await connection.fetchrow(sql, *args)
            except TimeoutError as error:
                raise QueryTimeoutError() from error

        if record is None:
            raise ValueError()

        return record

    async def fetch_rows(self, sql: str, *args: Any) -> list[Record]:  # noqa: ANN401
        async with self.acquire_connection() as connection:
            try:
                records: list[Record] = await connection.fetch(sql, *args)
            except TimeoutError as error:
                raise QueryTimeoutError() from error

        return records

    async def fetch_batch(
        self,
        sql: str,
        *args: Any,  # noqa: ANN401
        batch_size: int = 10000,
    ) -> AsyncIterator[list[Record]]:
        if self.pool_extraction is None:
            raise ValueError("Database connection pool for extraction not initialized")

        async with (
            self.acquire_connection() as connection,
            asyncio.timeout(CONFIG.ohsomedb.timeout_extraction),
            connection.transaction(readonly=True),
        ):
            batch: list[Record] = []
            try:
                async for record in connection.cursor(sql, *args, prefetch=batch_size):
                    batch.append(record)
                    if len(batch) >= batch_size:
                        yield batch
                        batch = []
            except TimeoutError as error:
                raise QueryTimeoutError() from error

            yield batch


db = Database()
