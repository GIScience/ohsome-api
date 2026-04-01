from pathlib import Path
from typing import Iterable

import psycopg
import pytest
from _pytest.monkeypatch import MonkeyPatch
from psycopg import Connection
from testcontainers.core.image import DockerImage
from testcontainers.postgres import PostgresContainer


@pytest.fixture
def ohsomedb_image() -> Iterable[DockerImage]:
    test_resource_path = Path(__file__).parent / "resources"
    with DockerImage(
        path=test_resource_path,
        clean_up=False,
        tag="ohsomedb-testcontainer",
    ) as image:
        yield image


@pytest.fixture
def ohsomedb_testcontainer(ohsomedb_image: DockerImage, monkeypatch: MonkeyPatch):
    with PostgresContainer(ohsomedb_image.short_id, driver=None) as postgres:
        monkeypatch.setattr(
            "ohsome_api.service.CONNECTION_STRING",
            postgres.get_connection_url(),
        )
        monkeypatch.setattr(
            "ohsome_api.db.CONNECTION_STRING",
            postgres.get_connection_url(),
        )
        yield postgres


@pytest.fixture
def ohsomedb_connection(
    ohsomedb_testcontainer: PostgresContainer,
) -> Iterable[Connection]:
    with psycopg.connect(ohsomedb_testcontainer.get_connection_url()) as connection:
        yield connection
