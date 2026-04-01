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
