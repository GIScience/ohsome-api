from pathlib import Path
from typing import Iterable

import pytest
from _pytest.monkeypatch import MonkeyPatch
from testcontainers.core.image import DockerImage
from testcontainers.postgres import PostgresContainer


@pytest.fixture(scope="session")
def ohsomedb_image() -> Iterable[DockerImage]:
    test_resource_path = Path(__file__).parent / "resources"
    with DockerImage(
        path=test_resource_path,
        clean_up=False,
        tag="ohsomedb-testcontainer",
    ) as image:
        yield image


@pytest.fixture(scope="session")
def ohsomedb_testcontainer(ohsomedb_image: DockerImage):
    with (
        PostgresContainer(ohsomedb_image.short_id, driver=None) as postgres,
        MonkeyPatch.context() as mp,
    ):
        mp.setattr(
            "ohsome_api.db.CONNECTION_STRING",
            postgres.get_connection_url(),
        )
        yield
