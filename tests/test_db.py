import pytest
from psycopg import Connection

from ohsome_api.db import get_latest_timestamp


def test_testcontainer(ohsomedb_connection: Connection):
    cur = ohsomedb_connection.execute("SELECT 1")
    result = cur.fetchone()
    assert result is not None
    assert result[0] == 1


@pytest.mark.usefixtures("ohsomedb_testcontainer")
def test_latest_timestamp():
    assert get_latest_timestamp().isoformat() == "2026-02-27T10:22:37+00:00"


def test_schema_present(ohsomedb_connection: Connection):
    query = "select schema_name from information_schema.schemata"
    cur = ohsomedb_connection.execute(query)
    result = cur.fetchall()
    assert "next" in (record[0] for record in result)
