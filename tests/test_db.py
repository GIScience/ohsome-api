from psycopg import Connection


def test_testcontainer(ohsomedb_connection: Connection):
    cur = ohsomedb_connection.execute("SELECT 1")
    result = cur.fetchone()
    assert result is not None
    assert result[0] == 1
