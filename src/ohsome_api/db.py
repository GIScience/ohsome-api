from datetime import datetime

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


async def fetch_row(sql: str) -> Record:
    # TODO: implement a resource manager for db connection
    connection: Connection = await asyncpg.connect(CONNECTION_STRING)
    record: Record = await connection.fetchrow(sql)
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


# TODO: ohsome filter support
async def get_contributions_count(query_where_clause: str, query_args: tuple) -> int:  # type: ignore
    sql = f"SELECT COUNT(*) as count FROM {CONFIG.schemaname}.contributions"  # noqa: S608
    record = await fetch_row(sql)
    return record["count"]
