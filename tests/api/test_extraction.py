import io
import json
from datetime import datetime

import pytest
from fastapi.testclient import TestClient
from pyarrow import Table, parquet
from pyarrow.parquet import FileMetaData
from shapely import from_wkb, to_wkt
from shapely.geometry import Polygon
from starlette.status import (
    HTTP_200_OK,
)

from ohsome_api.api import VERSION


def test_post_extraction_latest(
    client: TestClient,
    aoi_audimax: dict,
):
    json_body = {
        "filter": "id:node/1702635807",
        "time": "latest",
        "aoi": aoi_audimax,
    }
    response = client.post("/extraction/features.parquet", json=json_body)

    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    assert (
        response.headers["content-disposition"]
        == 'attachment; filename="features.parquet"'
    )

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)
    assert table.num_rows == 1

    # https://www.openstreetmap.org/api/0.6/node/1702635807
    validate_node_1702635807(table)

    metadata = parquet.read_metadata(response_file).metadata
    validate_ohsome_api_metadata(metadata)
    validate_geo_metadata_node_1702635807(metadata)


def test_get_extraction_latest(client: TestClient):
    parameters = {
        "filter": "id:node/1702635807",
        "aoi": "8.670919,49.416393,8.673839,49.417686",
        "time": "latest",
    }
    response = client.get("/extraction/features.parquet", params=parameters)

    # NOTE: Same asserts as previous test
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    assert (
        response.headers["content-disposition"]
        == 'attachment; filename="features.parquet"'
    )

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)
    assert table.num_rows == 1

    # https://www.openstreetmap.org/api/0.6/node/1702635807
    validate_node_1702635807(table)

    metadata = parquet.read_metadata(response_file).metadata
    validate_ohsome_api_metadata(metadata)
    validate_geo_metadata_node_1702635807(metadata)


def test_post_extraction_history_timestamp(client: TestClient, aoi_audimax: dict):
    response = client.post(
        "/extraction/features.parquet",
        json={
            "filter": "id:node/1702635807",
            "aoi": aoi_audimax,
            "time": "2017-09-23T00:00:00Z",
        },
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)

    assert table.num_rows == 1

    assert table["osm_id"][0].as_py() == 1702635807
    assert table["osm_type"][0].as_py() == "node"
    assert table["osm_version"][0].as_py() == 3
    assert table["osm_user_name"][0].as_py() == "ezelo"
    assert table["osm_changeset_id"][0].as_py() == 49417407
    assert ("name", "Einstein trifft Dürer auf Reisen") in table["osm_tags"][0].as_py()


def test_post_extraction_history_time_range(client: TestClient, aoi_audimax: dict):
    response = client.post(
        "/extraction/features.parquet",
        json={
            "filter": "id:node/1702635807",
            "aoi": aoi_audimax,
            "time": {
                "start": "2017-09-23T00:00:00Z",
                "end": "2017-10-30T00:00:00Z",
            },
        },
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)

    assert table.num_rows == 2

    assert table["osm_id"][0].as_py() == 1702635807
    assert table["osm_type"][0].as_py() == "node"
    assert table["osm_version"][0].as_py() == 3
    assert table["osm_user_name"][0].as_py() == "ezelo"
    assert table["osm_changeset_id"][0].as_py() == 49417407
    assert ("name", "Einstein trifft Dürer auf Reisen") in table["osm_tags"][0].as_py()

    assert table["osm_id"][1].as_py() == 1702635807
    assert table["osm_type"][1].as_py() == "node"
    assert table["osm_version"][1].as_py() == 4
    assert table["osm_user_name"][1].as_py() == "Tiamate"
    assert table["osm_changeset_id"][1].as_py() == 53333618
    assert ("name", "Dürer trifft Einstein auf Reisen") in table["osm_tags"][1].as_py()


def test_post_extraction_not_clipped(
    client: TestClient,
    aoi_audimax: dict,
):
    """Check if feature has been clipped."""
    response = client.post(
        "/extraction/features.parquet",
        json={
            "filter": "id:way/25961914",
            "time": "latest",
            "aoi": aoi_audimax,
            "clip": False,
        },
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


def test_post_extracmion_clipped(
    client: TestClient,
    aoi_audimax: dict,
):
    """Check if feature has been clipped."""
    response = client.post(
        "/extraction/features.parquet",
        json={
            "filter": "id:way/25961914",
            "time": "latest",
            "aoi": aoi_audimax,
        },
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


def test_post_extraction_deleted_features(
    client: TestClient,
    aoi_heigit: dict,
):
    response = client.post(
        "/extraction/features.parquet",
        json={
            "filter": "id:way/394983845",
            "time": "latest",
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    table = parquet.read_table(io.BytesIO(response.content))
    assert table.num_rows == 0


def test_post_extraction_route(client: TestClient):
    json_body = {
        "filter": "type=route and route=bus and service=night",
        "time": "latest",
        "aoi": "POLYGON ((8.6723140 49.4197687,8.6764091 49.4197687,8.6764091 49.4165896,8.6723140 49.4165896,8.6723140 49.4197687))",  # noqa: E501
    }

    response = client.post("/extraction/collections.parquet", json=json_body)
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)

    table = parquet.read_table(response_file)
    assert table.num_rows == 3


@pytest.mark.skip("Fails against test but not prod db.")
def test_post_extraction_route_history(client: TestClient):
    # https://www.openstreetmap.org/relation/57255/history/56
    json_body = {
        "filter": "id:relation/57255",
        "aoi": "POLYGON ((8.6723140 49.4197687,8.6764091 49.4197687,8.6764091 49.4165896,8.6723140 49.4165896,8.6723140 49.4197687))",  # noqa: E501
        "time": "2025-12-15T00:00:00Z",
        "clip": False,
    }

    response = client.post("/extraction/collections.parquet", json=json_body)
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)

    table = parquet.read_table(response_file)
    assert table.num_rows == 2  # one for points, one for lines

    for i in (0, 1):
        assert table["osm_id"][i].as_py() == 57255
        assert table["osm_type"][i].as_py() == "relation"
        assert table["osm_version"][i].as_py() == 56
        assert table["osm_user_name"][i].as_py() == "tyr_asd"
        assert table["osm_changeset_id"][i].as_py() == 175922956
        assert ("ref", "37") in table["osm_tags"][i].as_py()

    assert table["geom_type"][0].as_py() == "Point"
    assert table["geom_type"][1].as_py() == "LineString"


def test_extraction_route_points_only(client: TestClient):
    response = client.post(
        "/extraction/collections.parquet",
        json={
            "filter": "type=route and route=bus and service=night",
            "time": "latest",
            "member_filter": "geometry:point",
            "aoi": "POLYGON ((8.6723140 49.4197687,8.6764091 49.4197687,8.6764091 49.4165896,8.6723140 49.4165896,8.6723140 49.4197687))",  # noqa: E501
        },
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)

    table = parquet.read_table(response_file)
    assert table.num_rows == 1


@pytest.mark.skip("Fails against test but not prod db.")
def test_post_extraction_route_members(client: TestClient):
    response = client.post(
        "/extraction/collections_members.parquet",
        json={
            "filter": "id:relation/57255",
            "aoi": "POLYGON ((8.6723140 49.4197687,8.6764091 49.4197687,8.6764091 49.4165896,8.6723140 49.4165896,8.6723140 49.4197687))",  # noqa: E501
            "time": "2025-12-15T00:00:00Z",
            "clip": False,
        },
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)

    table = parquet.read_table(response_file)
    assert table.num_rows == 132


def test_extraction_route_members_no_clip(
    client: TestClient,
):
    json_body = {
        "filter": "type=route and route=bus and service=night",
        "clip": False,
        "time": "latest",
        "aoi": "POLYGON ((8.6723140 49.4197687,8.6764091 49.4197687,8.6764091 49.4165896,8.6723140 49.4165896,8.6723140 49.4197687))",  # noqa: E501
    }
    response = client.post(
        "/extraction/collections_members.parquet",
        json=json_body,
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)

    table = parquet.read_table(response_file)
    assert table.num_rows == 70


def test_post_extraction_route_members_no_clip_points_only(client: TestClient):
    response = client.post(
        "/extraction/collections_members.parquet",
        json={
            "filter": "type=route and route=bus and service=night",
            "time": "latest",
            "member_filter": "geometry:point",
            "clip": False,
            "aoi": "POLYGON ((8.6723140 49.4197687,8.6764091 49.4197687,8.6764091 49.4165896,8.6723140 49.4165896,8.6723140 49.4197687))",  # noqa: E501
        },
    )
    assert response.status_code == HTTP_200_OK

    response_file = io.BytesIO(response.content)

    table = parquet.read_table(response_file)
    assert table.num_rows == 31


def validate_node_1702635807(table: Table):
    # https://www.openstreetmap.org/api/0.6/node/1702635807

    assert table.num_rows == 1

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
    assert table["edit_timestamp"][0].as_py() == last_edit_expected

    geom = table["geom"][0].as_py()
    geom = from_wkb(geom)
    assert to_wkt(geom) == "POINT (8.672892 49.416696)"


def validate_geo_metadata_node_1702635807(metadata: FileMetaData):
    metadata = metadata[b"geo"].decode("utf-8")
    metadata = json.loads(metadata)
    assert metadata["columns"]["geom"]["bbox"] == [
        8.6728921,
        49.4166963,
        8.6728921,
        49.4166963,
    ]
    assert "crs" in metadata["columns"]["geom"]


def validate_ohsome_api_metadata(metadata: FileMetaData):
    metadata = metadata[b"ohsome API"].decode("utf-8")
    metadata = json.loads(metadata)
    assert metadata["version"] == VERSION
    assert metadata["attribution"]["url"] == "https://ohsome.org/copyrights"
    assert metadata["attribution"]["text"] == "© OpenStreetMap contributors"
