from typing import Iterable

import psycopg
import pytest
from psycopg import Connection
from testcontainers.postgres import PostgresContainer


@pytest.fixture
def ohsomedb_testcontainer() -> Iterable[PostgresContainer]:
    with PostgresContainer("citusdata/citus:latest", driver=None) as postgres:
        yield postgres


@pytest.fixture
def ohsomedb_connection(
    ohsomedb_testcontainer: PostgresContainer,
) -> Iterable[Connection]:
    with psycopg.connect(ohsomedb_testcontainer.get_connection_url()) as connection:
        yield connection


def test_testcontainer(ohsomedb_connection: Connection):
    cur = ohsomedb_connection.execute("SELECT 1")
    result = cur.fetchone()
    assert result is not None
    assert result[0] == 1
