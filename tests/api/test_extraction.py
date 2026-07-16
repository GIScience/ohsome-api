import io
import json
from datetime import datetime
from typing import Literal

import pytest
from fastapi.testclient import TestClient
from pyarrow import parquet
from shapely import from_wkb, to_wkt
from shapely.geometry import Polygon
from starlette.status import (
    HTTP_200_OK,
)

from ohsome_api.api import VERSION


@pytest.mark.parametrize("timestamp", ["latest", None])
def test_extraction_latest(
    client: TestClient,
    aoi_audimax: dict,
    timestamp: Literal["latest"] | None,
):
    json_body = {
        "filter": "id:node/1702635807",
        "aoi": aoi_audimax,
    }
    if timestamp is not None:
        json_body["timestamp"] = timestamp
    response = client.post("/extraction/features.parquet", json=json_body)
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    assert (
        response.headers["content-disposition"]
        == 'attachment; filename="extractions.parquet"'
    )

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)
    assert table.num_rows == 1

    # https://www.openstreetmap.org/api/0.6/node/1702635807
    assert table["osm_id"][0].as_py() == 1702635807
    assert table["osm_type"][0].as_py() == "node"
    assert table["osm_version"][0].as_py() == 6
    assert table["osm_user_name"][0].as_py() == "ezelo"
    assert table["osm_changeset_id"][0].as_py() == 74974721
    assert table["osm_tags"][0].as_py() == [
        ("name", "Dürer trifft Einstein auf Reisen"),
        ("tourism", "artwork"),
        ("material", "bronze"),
        ("start_date", "2011"),
        ("wheelchair", "yes"),
        ("artist_name", "Sabrina Hohmann"),
        ("artwork_type", "sculpture"),
    ]
    assert table["minor_version"][0].as_py() == 0
    assert table["clipped"][0].as_py() is False

    last_edit_expected = datetime.fromisoformat("2019-09-26 17:18:15.000000Z")
    assert table["last_edit"][0].as_py() == last_edit_expected

    geom = table["geom"][0].as_py()
    geom = from_wkb(geom)
    assert to_wkt(geom) == "POINT (8.672892 49.416696)"

    metadata = parquet.read_metadata(response_file).metadata
    metadata_geo = metadata[b"geo"].decode("utf-8")
    metadata_geo = json.loads(metadata_geo)

    assert metadata_geo["columns"]["geom"]["bbox"] == [
        8.6728921,
        49.4166963,
        8.6728921,
        49.4166963,
    ]
    assert "crs" in metadata_geo["columns"]["geom"]

    metadata_api = metadata[b"api"].decode("utf-8")
    metadata_api = json.loads(metadata_api)
    assert metadata_api["version"] == VERSION
    assert metadata_api["attribution"]["url"] == "https://ohsome.org/copyrights"
    assert metadata_api["attribution"]["text"] == "© OpenStreetMap contributors"


def test_extraction_history(client: TestClient, aoi_audimax: dict):
    response = client.post(
        "/extraction/features.parquet",
        json={
            "filter": "id:node/1702635807",
            "aoi": aoi_audimax,
            "timestamp": "2017-09-23T00:00:00Z",
        },
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)

    assert table["osm_id"][0].as_py() == 1702635807
    assert table["osm_type"][0].as_py() == "node"
    assert table["osm_version"][0].as_py() == 3
    assert table["osm_user_name"][0].as_py() == "ezelo"
    assert table["osm_changeset_id"][0].as_py() == 49417407
    assert ("name", "Einstein trifft Dürer auf Reisen") in table["osm_tags"][0].as_py()


def test_extraction_not_clipped(
    client: TestClient,
    aoi_audimax: dict,
):
    """Check if feature has been clipped."""
    response = client.post(
        "/extraction/features.parquet",
        json={"filter": "id:way/25961914", "aoi": aoi_audimax, "clip": False},
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)
    assert table.num_rows == 1
    assert table["clipped"][0].as_py() is False

    geom = table["geom"][0].as_py()
    geom_clipped: Polygon = from_wkb(geom)
    # 21 nodes derived from https://www.openstreetmap.org/way/25961914
    assert len(geom_clipped.exterior.coords) == 21


def test_extraction_clipped(
    client: TestClient,
    aoi_audimax: dict,
):
    """Check if feature has been clipped."""
    response = client.post(
        "/extraction/features.parquet",
        json={"filter": "id:way/25961914", "aoi": aoi_audimax},
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)
    assert table.num_rows == 1
    assert table["clipped"][0].as_py() is True

    geom = table["geom"][0].as_py()
    geom_clipped: Polygon = from_wkb(geom)
    # 21 nodes derived from https://www.openstreetmap.org/way/25961914
    assert len(geom_clipped.exterior.coords) < 21


def test_extraction_deleted_features(
    client: TestClient,
    aoi_heigit: dict,
):
    response = client.post(
        "/extraction/features.parquet",
        json={"filter": "id:way/394983845", "aoi": aoi_heigit},
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    table = parquet.read_table(io.BytesIO(response.content))
    assert table.num_rows == 0
