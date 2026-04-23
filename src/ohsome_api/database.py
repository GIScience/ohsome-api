from typing import Any

import asyncpg
from asyncpg import Record

from ohsome_api.config import CONFIG

CONNECTION_STRING = CONFIG.ohsomedb.connection_string


class Database:
    def __init__(self) -> None:
        self.pool: asyncpg.Pool | None = None

    async def connect(self) -> None:
        # Initialize the pool once
        self.pool = await asyncpg.create_pool(
            dsn=CONNECTION_STRING,
            min_size=5,
            max_size=20,
        )
        print("Database connection pool established")

    async def disconnect(self) -> None:
        if self.pool:
            await self.pool.close()
            print("Database connection pool closed")

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


db = Database()
