import pytest
from fastapi.testclient import TestClient
from starlette.status import (
    HTTP_200_OK,
    HTTP_404_NOT_FOUND,
    HTTP_422_UNPROCESSABLE_CONTENT,
)

from ohsome_api.api import app


@pytest.fixture(scope="module")
def client():
    with TestClient(app) as client:
        yield client


pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer")]


def test_metadata(client: TestClient):
    response = client.get("/metadata")
    assert response.status_code == HTTP_200_OK
    assert response.json() == {
        "latestTimestamp": "2026-02-27T10:22:37+00:00",
        "apiVersion": "0.0.0",
        "attribution": {
            "url": "https://ohsome.org/copyrights",
            "text": "© OpenStreetMap contributors",
        },
    }


def test_contributions_count_as_json(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/contributions/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"][0]["value"] == 4
    assert len(response.json()["result"]) == 1


def test_contributions_count_as_json_time_bin_size(
    client: TestClient, aoi_geojson_heigit: dict
):
    response = client.post(
        "/contributions/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {
                "start": "2024-01-01",
                "end": "2025-12-31",
                "binSize": "P1Y",
            },
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert len(response.json()["result"]) == 2
    assert response.json()["result"][1]["value"] == 4


def test_contributions_count_as_csv(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/contributions/count.csv",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    expected_result = """# apiVersion: 0.0.0
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start,end,value
2024-01-01T00:00:00Z,2025-12-31T00:00:00Z,4
"""
    # TODO: Why no timezone in response?
    assert response.text == expected_result


def test_contributions_count_with_invalid_filter(
    client: TestClient, aoi_geojson_heigit: dict
):
    response = client.post(
        "/contributions/count.json",
        json={
            "filter": "foo",
            "timeBins": {"start": "2025-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT
    # TODO: Check error message and make it user friendly


@pytest.mark.parametrize(
    "case",
    [
        "2025-02-31",  # invalid date
        "",  # empty
        "foo",  # garbage
    ],
)
def test_contributions_count_with_invalid_time(
    client: TestClient, case: str, aoi_geojson_heigit: dict
):
    response = client.post(
        "/contributions/count.json",
        json={
            "filter": "id:1",
            "timeBins": {"start": case, "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT


def test_contributions_count_without_format(
    client: TestClient, aoi_geojson_heigit: dict
):
    response = client.post(
        "/contributions/count",
        json={
            "filter": "id:1",
            "timeBins": {"start": "2025-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_404_NOT_FOUND


# TODO: extract to own module indepedend of testcontainer
def test_time_request(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/contributions/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {
                "start": "2025-01-01",
                "end": "2025-12-31T00:00Z",
            },
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
