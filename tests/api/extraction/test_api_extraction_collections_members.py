import io

import pytest
from pyarrow import parquet
from starlette.status import HTTP_200_OK
from starlette.testclient import TestClient


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
