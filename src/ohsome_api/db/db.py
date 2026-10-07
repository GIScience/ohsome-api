# ruff: noqa: ANN401

import asyncio
import json
import logging
from contextlib import asynccontextmanager
from datetime import datetime
from pathlib import Path
from typing import Any, AsyncIterator

import asyncpg
from asyncpg import Connection, Pool, Record
from shapely.geometry.base import BaseGeometry
from shapely.wkb import dumps as dump_wkb

from ohsome_api.config import CONFIG
from ohsome_api.db.errors import PoolAcquireTimeoutError, QueryTimeoutError

CONNECTION_STRING = CONFIG.ohsomedb.connection_string
SCHEMA = CONFIG.ohsomedb.schemaname

logger = logging.getLogger("ohsome-api")


def convert_datetime_to_timestamp(input_: Any) -> Any:
    if isinstance(input_, list | tuple):
        return [convert_datetime_to_timestamp(i) for i in input_]
    if isinstance(input_, datetime):
        return input_.isoformat()
    else:
        return input_


def encode_geometry(geometry: BaseGeometry) -> bytes:
    return dump_wkb(geometry, srid=4326)


async def init_connection(connection: Connection) -> None:
    await connection.set_type_codec(
        "jsonb",
        encoder=(lambda x: x),
        decoder=json.loads,
        schema="pg_catalog",
    )
    await connection.set_type_codec(
        "geometry",
        encoder=encode_geometry,
        decoder=(lambda x: x),
        format="binary",
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
            init=init_connection,
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
            init=init_connection,
            command_timeout=CONFIG.ohsomedb.timeout_extraction,  # query timeout
            server_settings={
                "application_name": "ohsome-api",
                "search_path": f"{SCHEMA},public",
                # Does not work as long as
                # "SET citus.propagate_set_commands = 'local';"
                # is not set on the database level
                # "work_mem": CONFIG.ohsomedb.work_mem
            },
        )
        logging.info("Database connection pools established.")

    async def disconnect(self) -> None:
        if self.pool is not None:
            await self.pool.close()

        if self.pool_extraction is not None:
            await self.pool_extraction.close()
        logging.info("Database connection pools closed.")

    @asynccontextmanager
    async def acquire_connection(
        self,
        pool: Pool | None,
        timeout: int = CONFIG.ohsomedb.pool_acquire_timeout,
    ) -> AsyncIterator[Connection]:
        if pool is None:
            raise ConnectionError("Database connection pool not initialized.")

        acquiring = True
        try:
            async with pool.acquire(timeout=timeout) as connection:
                acquiring = False
                yield connection
        except TimeoutError as error:
            # Only raise custom error if TimeoutError is thrown during acquiring
            if acquiring:
                raise PoolAcquireTimeoutError(
                    "Could not acquire database connection. "
                    + "The service is temporarily busy. "
                    + "Please retry shortly."
                ) from error
            raise

    async def fetch_row(self, sql: str, *args: Any) -> Record:
        async with self.acquire_connection(self.pool) as connection:
            try:
                if CONFIG.ohsomedb.debug:
                    await self.debug_query(connection, sql, *args)
                record: Record = await connection.fetchrow(sql, *args)
            except TimeoutError as error:
                raise QueryTimeoutError() from error

        if record is None:
            raise ValueError()

        return record

    async def fetch_rows(self, sql: str, *args: Any) -> list[Record]:
        async with self.acquire_connection(self.pool) as connection:
            # TODO why can't we set this in the pool settings? It seems to be ignored there.  # noqa: E501
            # TODO: This should be done in the database setup
            await connection.execute("SET citus.propagate_set_commands = 'local';")
            try:
                async with connection.transaction(readonly=True):
                    await connection.execute(
                        f"SET LOCAL work_mem = '{CONFIG.ohsomedb.work_mem}';"
                    )
                    await connection.execute("SET LOCAL jit = off;")
                    if CONFIG.ohsomedb.debug:
                        await self.debug_query(connection, sql, *args)
                    records: list[Record] = await connection.fetch(sql, *args)
            except TimeoutError as error:
                raise QueryTimeoutError() from error

        return records

    async def fetch_batch(
        self, sql: str, *args: Any, batch_size: int
    ) -> AsyncIterator[list[Record]]:
        async with (
            self.acquire_connection(self.pool_extraction) as connection,
            asyncio.timeout(CONFIG.ohsomedb.timeout_extraction),
            connection.transaction(readonly=True),
        ):
            batch: list[Record] = []
            try:
                if CONFIG.ohsomedb.debug:
                    await self.debug_query(connection, sql, *args)
                async for record in connection.cursor(sql, *args, prefetch=batch_size):
                    batch.append(record)
                    if len(batch) >= batch_size:
                        yield batch
                        batch = []
            except TimeoutError as error:
                raise QueryTimeoutError() from error

            yield batch

    async def explain(
        self,
        connection: Connection,
        sql: str,
        *args: Any,
        analyze: bool = False,
    ) -> str:
        explain_sql = "EXPLAIN "
        if analyze:
            explain_sql += "(analyse, buffers, verbose, costs)\n"
        explain_sql += sql

        logger.info("SQL Query: \n" + explain_sql)

        # await connection.execute("SET citus.explain_all_tasks = on;")
        result = await connection.fetch(explain_sql, *args)
        return "\n".join(r["QUERY PLAN"] for r in result)

    async def debug_query(self, connection: Connection, sql: str, *args: Any) -> None:
        plan = await self.explain(connection, sql, *args, analyze=True)

        hash_ = hash(sql)
        basepath = Path(f"debug_sql_{hash_}")
        basepath.with_suffix("sql").write_text(sql)
        basepath.with_suffix("plan").write_text(plan)

        logger.info(f"Query and plan written to {basepath}")
        logger.info("Args: \n" + str(convert_datetime_to_timestamp(args)))


db = Database()
