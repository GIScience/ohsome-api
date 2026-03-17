from typing import Iterable

import psycopg
import pytest
from psycopg import Connection
from testcontainers.postgres import PostgresContainer


@pytest.fixture
def ohsomedb_testcontainer(
    monkeypatch: pytest.MonkeyPatch,
) -> Iterable[PostgresContainer]:
    with PostgresContainer("citusdata/citus:latest", driver=None) as postgres:
        monkeypatch.setattr(
            "ohsome_api.service.CONNECTION_STRING",
            postgres.get_connection_url(),
        )
        yield postgres


@pytest.fixture
def ohsomedb_connection(
    ohsomedb_testcontainer: PostgresContainer,
) -> Iterable[Connection]:
    with psycopg.connect(ohsomedb_testcontainer.get_connection_url()) as connection:
        yield connection
