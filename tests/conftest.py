import json
from pathlib import Path
from typing import Iterable

import pytest
import pytest_asyncio
from _pytest.monkeypatch import MonkeyPatch
from geojson_pydantic.geometries import parse_geometry_obj
from testcontainers.core.image import DockerImage
from testcontainers.postgres import PostgresContainer

from ohsome_api.db.db import db
from ohsome_api.request_models.aoi import BBox


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
            "ohsome_api.db.db.CONNECTION_STRING",
            postgres.get_connection_url(),
        )
        yield


@pytest.fixture(scope="session", autouse=True)
def ohsomedb_schema():
    with MonkeyPatch.context() as mp:
        mp.setattr("ohsome_api.db.db.SCHEMA", "current")
        yield


@pytest_asyncio.fixture
async def database_pool():
    await db.connect()
    yield
    await db.disconnect()


@pytest_asyncio.fixture
async def expected_api_version():
    return "2.0.0rc2"


@pytest.fixture
def aoi_geojson_heigit():
    # Small bounding box around HeiGIT in Heidelberg, Germany
    return {
        "type": "Polygon",
        "coordinates": [
            [
                [8.674585743714516, 49.418922925485816],
                [8.674585743714516, 49.417888246956096],
                [8.676354634855528, 49.417888246956096],
                [8.676354634855528, 49.418922925485816],
                [8.674585743714516, 49.418922925485816],
            ]
        ],
    }


@pytest.fixture
def aoi_geojson_audimax():
    # Small bounding box around Audimax in Heidelberg, Germany
    return {
        "type": "Polygon",
        "coordinates": [
            [
                [8.670919, 49.417686],
                [8.673839, 49.417686],
                [8.673727, 49.416393],
                [8.671120, 49.416393],
                [8.670919, 49.417686],
            ]
        ],
    }


@pytest.fixture
def aoi_bbox_heigit() -> tuple[float, float, float, float]:
    """Bounding box as tuple/array for POST requests."""
    return (8.674585, 49.417888, 8.676354, 49.418922)


@pytest.fixture
def aoi_bbox_audimax() -> tuple[float, float, float, float]:
    """Bounding box as tuple/array for POST requests."""
    return (8.670919, 49.416393, 8.673839, 49.417686)


@pytest.fixture
def aoi_bbox_as_str_audimax() -> str:
    return "8.670919,49.416393,8.673839,49.417686"


@pytest.fixture
def aoi_wkt_heigit(aoi_geojson_heigit: dict) -> str:
    parsed = parse_geometry_obj(aoi_geojson_heigit)
    return parsed.wkt


@pytest.fixture
def aoi_wkt_multipolygon() -> str:
    return "MULTIPOLYGON (((8.673015 49.418177, 8.675053 49.418177, 8.675053 49.41914, 8.673015 49.41914, 8.673015 49.418177)), ((8.667394 49.415594, 8.67029 49.415594, 8.67029 49.416711, 8.667394 49.416711, 8.667394 49.415594)))"  # noqa: E501


@pytest.fixture
def aoi_wkt_audimax(aoi_geojson_audimax: dict) -> str:
    parsed = parse_geometry_obj(aoi_geojson_audimax)
    return parsed.wkt


@pytest.fixture
def aoi_str_geojson_heigit(aoi_geojson_heigit: dict) -> str:
    return json.dumps(aoi_geojson_heigit)


@pytest.fixture
def aoi_str_geojson_audimax(aoi_geojson_audimax: dict) -> str:
    return json.dumps(aoi_geojson_audimax)


@pytest.fixture
def aoi_str_bbox_heigit(aoi_bbox_heigit: BBox) -> str:
    return json.dumps(aoi_bbox_heigit)


@pytest.fixture
def aoi_str_bbox_audimax(aoi_bbox_audimax: BBox) -> str:
    return json.dumps(aoi_bbox_audimax)


@pytest.fixture(
    params=[
        "aoi_geojson_heigit",
        "aoi_bbox_heigit",
        "aoi_wkt_heigit",
        "aoi_str_geojson_heigit",
        "aoi_str_bbox_heigit",
    ],
)
def aoi_heigit(request: pytest.FixtureRequest) -> dict | tuple:
    return request.getfixturevalue(request.param)


@pytest.fixture(
    params=[
        "aoi_geojson_audimax",
        "aoi_bbox_audimax",
        "aoi_wkt_audimax",
        "aoi_str_geojson_audimax",
        "aoi_str_bbox_audimax",
    ],
)
def aoi_audimax(request: pytest.FixtureRequest) -> dict | tuple:
    return request.getfixturevalue(request.param)
