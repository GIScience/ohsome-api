import json
import logging
from typing import Any, AsyncIterator

import asyncpg
from asyncpg import Connection, Record

from ohsome_api.config import CONFIG

CONNECTION_STRING = CONFIG.ohsomedb.connection_string

logger = logging.getLogger("ohsome-api")


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

    async def connect(self) -> None:
        # Initialize the pool once
        self.pool = await asyncpg.create_pool(
            dsn=CONNECTION_STRING,
            min_size=5,
            max_size=20,
            init=jsonb_codec,
        )
        logging.info("Database connection pool established.")

    async def disconnect(self) -> None:
        if self.pool:
            await self.pool.close()
            logging.info("Database connection pool closed.")

    async def fetch_row(self, sql: str, *args: Any) -> Record:  # noqa: ANN401
        if self.pool is None:
            raise ValueError("Database connection pool not initialized")

        async with self.pool.acquire() as connection:
            record: Record = await connection.fetchrow(sql, *args)

        if record is None:
            raise ValueError()

        return record

    async def fetch_rows(self, sql: str, *args: Any) -> list[Record]:  # noqa: ANN401
        if self.pool is None:
            raise ValueError("Database connection pool not initialized")

        async with self.pool.acquire() as connection:
            records: list[Record] = await connection.fetch(sql, *args)

        return records

    async def fetch_batch(  # noqa: C901
        self,
        sql: str,
        *args: Any,  # noqa: ANN401
        batch_size: int = 10000,
    ) -> AsyncIterator[list[Record]]:
        if self.pool is None:
            raise ValueError("Database connection pool not initialized")

        async with self.pool.acquire() as connection, connection.transaction():
            batch: list[Record] = []
            async for record in connection.cursor(sql, *args, prefetch=batch_size):
                batch.append(record)
                if len(batch) >= batch_size:
                    yield batch
                    batch = []

            if batch:
                yield batch


db = Database()
