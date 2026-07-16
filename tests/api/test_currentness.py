import pytest
from fastapi.testclient import TestClient
from starlette.status import (
    HTTP_200_OK,
    HTTP_404_NOT_FOUND,
    HTTP_422_UNPROCESSABLE_CONTENT,
)

from ohsome_api.api import VERSION


def test_currentness_count_as_json(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"]["value"][0] == 2
    assert len(response.json()["result"]["value"]) == 1


def test_currentness_length_as_json(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/length.json",
        json={
            "filter": "highway=* and geometry:line",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"]["value"][0] == 30
    assert len(response.json()["result"]["value"]) == 1


def test_currentness_area_as_json(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/stats/currentness/area.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"]["value"][0] == 770
    assert len(response.json()["result"]["value"]) == 1


def test_currentness_as_json_time_bin_size(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {
                "start": "2024-01-01",
                "end": "2025-12-31",
                "binSize": "P1Y",
            },
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert len(response.json()["result"]["value"]) == 2
    assert response.json()["result"]["value"][1] == 2


def test_currentness_as_csv(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count.csv",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2023-01-01", "end": "2025-12-31", "binSize": "P1Y"},
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    expected_result = f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start;end;value
2023-01-01T00:00:00Z;2024-01-01T00:00:00Z;0
2024-01-01T00:00:00Z;2025-01-01T00:00:00Z;0
2025-01-01T00:00:00Z;2025-12-31T00:00:00Z;2
"""
    # TODO: Why no timezone in response?
    assert response.text == expected_result


def test_currentness_with_invalid_filter(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count.json",
        json={
            "filter": "foo",
            "timeBins": {"start": "2025-01-01", "end": "2025-12-31"},
            "aoi": aoi_heigit,
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
def test_currentness_with_invalid_time(client: TestClient, case: str, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count.json",
        json={
            "filter": "id:1",
            "timeBins": {"start": case, "end": "2025-12-31"},
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT


def test_currentness_without_format(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count",
        json={
            "filter": "id:1",
            "timeBins": {"start": "2025-01-01", "end": "2025-12-31"},
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_404_NOT_FOUND


# TODO: extract to own module indepedend of testcontainer
def test_time_request(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {
                "start": "2025-01-01",
                "end": "2025-12-31T00:00Z",
            },
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK


def test_time_bins_with_bin_size_none(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/stats/currentness/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {
                "start": "2025-01-01",
                "end": "2025-12-31T00:00Z",
                "binSize": None,
            },
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK


# TODO: extract to own module
def test_invalid_topology_aoi(client: TestClient, aoi_geojson_invalid_topology: dict):
    response = client.post(
        "/stats/currentness/area.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_invalid_topology,
        },
    )
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT
