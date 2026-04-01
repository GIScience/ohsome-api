from datetime import datetime

import psycopg
from psycopg.sql import SQL

CONNECTION_STRING: str = ""


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
