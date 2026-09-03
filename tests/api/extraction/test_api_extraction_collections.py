import io

import pytest
from pyarrow import parquet
from starlette.status import HTTP_200_OK
from starlette.testclient import TestClient


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
        assert table["version"][i].as_py() == 56
        assert table["user_name"][i].as_py() == "tyr_asd"
        assert table["changeset_id"][i].as_py() == 175922956
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
