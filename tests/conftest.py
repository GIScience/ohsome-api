from pathlib import Path
from typing import Iterable

import pytest
import pytest_asyncio
from _pytest.monkeypatch import MonkeyPatch
from testcontainers.core.image import DockerImage
from testcontainers.postgres import PostgresContainer

from ohsome_api.database import db
from ohsome_api.request_models import GeoJsonFeatureCollection


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


@pytest.fixture(scope="session", autouse=True)
def ohsomedb_schema():
    with MonkeyPatch.context() as mp:
        mp.setattr("ohsome_api.db.SCHEMA", "current")
        yield


@pytest_asyncio.fixture
async def database_pool():
    await db.connect()
    yield
    await db.disconnect()


@pytest.fixture
def aoi_geojson_heigit():
    # Small bounding box around HeiGIT
    return {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {},
                "geometry": {
                    "coordinates": [
                        [
                            [8.674585743714516, 49.418922925485816],
                            [8.674585743714516, 49.417888246956096],
                            [8.676354634855528, 49.417888246956096],
                            [8.676354634855528, 49.418922925485816],
                            [8.674585743714516, 49.418922925485816],
                        ]
                    ],
                    "type": "Polygon",
                },
            }
        ],
    }


@pytest.fixture
def aoi_geojson_invalid_topology():
    # Small bounding box around HeiGIT
    return {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {},
                "geometry": {
                    "coordinates": [
                        [
                            [8.674585743714516, 49.418922925485816],
                            [8.676354634855528, 49.417888246956096],
                            [8.674585743714516, 49.417888246956096],
                            [8.676354634855528, 49.418922925485816],
                            [8.674585743714516, 49.418922925485816],
                        ]
                    ],
                    "type": "Polygon",
                },
            }
        ],
    }


@pytest.fixture
def aoi_wkt_heigit(aoi_geojson_heigit: dict) -> str:
    parsed = GeoJsonFeatureCollection(**aoi_geojson_heigit)
    return parsed.features[0].geometry.wkt
