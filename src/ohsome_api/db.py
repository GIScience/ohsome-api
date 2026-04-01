from datetime import datetime

import psycopg
from psycopg.sql import SQL
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


CONNECTION_STRING: str = Config().connection_string


def fetch_one(sql: SQL) -> tuple:
    with psycopg.connect(CONNECTION_STRING) as connection:
        with connection.cursor() as cursor:
            cursor.execute(sql)
            record = cursor.fetchone()
            if record is None:
                raise ValueError()
            else:
                return record


def get_latest_timestamp() -> datetime:
    sql = SQL("SELECT last_timestamp FROM next.contributions_state")
    record = fetch_one(sql)
    if not isinstance(record[0], datetime):
        raise TypeError()
    return record[0]


def get_contributions_count() -> int:
    sql = SQL("SELECT COUNT(*) FROM next.contributions")
    record = fetch_one(sql)
    return record[0]
