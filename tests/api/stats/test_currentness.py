from fastapi.testclient import TestClient
from starlette.status import (
    HTTP_200_OK,
)

from ohsome_api.api import VERSION


def test_currentness_count_as_json(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/currentness/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {"start": "2024-01-01", "end": "2025-12-31"},
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
            "time": {"start": "2024-01-01", "end": "2025-12-31"},
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
            "time": {"start": "2024-01-01", "end": "2025-12-31"},
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
            "time": {
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
            "time": {"start": "2023-01-01", "end": "2025-12-31", "binSize": "P1Y"},
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
