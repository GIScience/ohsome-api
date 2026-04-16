from datetime import datetime
from typing import Any

import asyncpg
from asyncpg import Connection, Record
from pydantic import computed_field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Config(BaseSettings):
    user: str = "ohsomedb"
    password: str = "ohsomedb"  # noqa: S105
    host: str = "localhost"
    port: int = 5432
    dbname: str = "ohsomedb"
    schemaname: str = "current"
    application_name: str = "ohsome-api"

    model_config = SettingsConfigDict(
        env_prefix="OHSOME_API_OHSOMEDB_",
        env_nested_delimiter="_",
    )

    @computed_field
    @property
    def connection_string(self) -> str:
        return "postgresql://{user}:{password}@{host}:{port}/{dbname}?application_name={application_name}".format(
            user=self.user,
            password=self.password,
            host=self.host,
            port=self.port,
            dbname=self.dbname,
            application_name=self.application_name,
        )


CONFIG = Config()
CONNECTION_STRING: str = CONFIG.connection_string


async def fetch_row(sql: str, *args: Any) -> Record:  # noqa: ANN401
    # TODO: implement a resource manager for db connection
    connection: Connection = await asyncpg.connect(CONNECTION_STRING)
    record: Record = await connection.fetchrow(sql, *args)
    await connection.close()
    if record is None:
        raise ValueError()
    else:
        return record


async def get_latest_timestamp() -> datetime:
    sql = f"SELECT last_timestamp FROM {CONFIG.schemaname}.contributions_state"  # noqa: S608
    record = await fetch_row(sql)
    if not isinstance(record["last_timestamp"], datetime):
        raise TypeError()
    return record["last_timestamp"]


async def get_contributions_count(
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    period: str | None,
) -> list[dict[str, int | datetime]]:
    filter_args_count = len(filter_args)
    sql = f"""
        SELECT COUNT(*) as count
        FROM {CONFIG.schemaname}.contributions
        WHERE {filter_where_clause}
        AND valid_from BETWEEN ${filter_args_count + 1} AND ${filter_args_count + 2}
    """  # noqa: S608
    record = await fetch_row(sql, *filter_args, start, end)  # order matters!
    return [
        {
            "value": record["count"],
            "start": start,
            "end": end,
        }
    ]
