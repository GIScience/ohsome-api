from pathlib import Path
from typing import Iterable

import pytest
import pytest_asyncio
from _pytest.monkeypatch import MonkeyPatch
from testcontainers.core.image import DockerImage
from testcontainers.postgres import PostgresContainer

from ohsome_api.database import db


@pytest.fixture(scope="session")
def ohsomedb_image() -> Iterable[DockerImage]:
    test_resource_path = Path(__file__).parent / "resources"
    with DockerImage(
        path=test_resource_path,
        clean_up=False,
        tag="ohsomedb-testcontainer",
        platform="linux/amd64",
    ) as image:
        yield image


@pytest.fixture(scope="session")
def ohsomedb_testcontainer(ohsomedb_image: DockerImage):
    with (
        PostgresContainer(ohsomedb_image.short_id, driver=None) as postgres,
        MonkeyPatch.context() as mp,
    ):
        mp.setattr(
            "ohsome_api.database.CONNECTION_STRING",
            postgres.get_connection_url(),
        )
        yield


@pytest_asyncio.fixture()
async def database_pool():
    await db.connect()
    yield
    await db.disconnect()
