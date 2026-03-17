import psycopg

CONNECTION_STRING: str = ""


def get_result() -> str:
    with psycopg.connect(CONNECTION_STRING) as connection:
        result = connection.execute("SELECT 'Database'").fetchone()
        if result is None:
            raise ValueError()
        return result[0]
