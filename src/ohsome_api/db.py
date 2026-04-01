from datetime import datetime

import psycopg

CONNECTION_STRING: str = ""


def get_latest_timestamp() -> datetime:
    with psycopg.connect(CONNECTION_STRING) as connection:
        query = "SELECT last_timestamp FROM next.contributions_state"
        cursor = connection.execute(query)
        record = cursor.fetchone()
        if record is None:
            raise ValueError()
        if not isinstance(record[0], datetime):
            raise TypeError()
        return record[0]


def get_contributions_count() -> int:
    query = "SELECT COUNT(*) FROM next.contributions"
    # TODO: factor out connection handling
    with psycopg.connect(CONNECTION_STRING) as connection:
        with connection.cursor() as cursor:
            cursor.execute(query)
            record = cursor.fetchone()
            if record is None:
                raise ValueError()
            return record[0]
