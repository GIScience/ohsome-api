import pytest
from fastapi.testclient import TestClient
from httpx2 import Response
from starlette.status import HTTP_200_OK


def test_features_as_json_time_series(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/features/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {
                "start": "2024-01-01",
                "end": "2025-12-31",
                "interval": "P1Y",
            },
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    result = response.json()["result"]
    assert result == {
        "timestamp": [
            "2024-01-01T00:00:00Z",
            "2025-01-01T00:00:00Z",
            "2025-12-31T00:00:00Z",
        ],
        "value": [3, 3, 4],
    }


def test_features_as_json_timestamp(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/features/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": "2024-01-01",
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    result = response.json()["result"]
    assert result == {
        "timestamp": [
            "2024-01-01T00:00:00Z",
        ],
        "value": [3],
    }


def test_features_as_json_multipolygon(client: TestClient, aoi_wkt_multipolygon: str):
    response = client.post(
        "/stats/features/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": "2024-01-01",
            "aoi": aoi_wkt_multipolygon,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    result = response.json()["result"]
    assert result == {
        "timestamp": [
            "2024-01-01T00:00:00Z",
        ],
        "value": [17],
    }


def test_features_group_by_as_json(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/features/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {
                "start": "2024-01-01",
                "end": "2025-12-31",
                "interval": "P1Y",
            },
            "aoi": aoi_heigit,
            "groupBy": {
                "type": "byTag",
                "key": "building",
            },
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    result = response.json()["result"]
    assert result == {
        "timestamp": [
            "2024-01-01T00:00:00Z",
            "2025-01-01T00:00:00Z",
            "2025-12-31T00:00:00Z",
        ],
        "value": [3, 3, 4],
        "values": {
            "university": [2, 2, 3],
            "greenhouse": [1, 1, 1],
        },
    }


def test_features_as_csv(
    client: TestClient, aoi_heigit: dict, expected_api_version: str
):
    response: Response = client.post(
        "/stats/features/count.csv",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {
                "start": "2024-01-01",
                "end": "2025-12-31",
                "interval": "P1Y",
            },
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    assert (
        response.text
        == f"""# apiVersion: {expected_api_version}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: \xa9 OpenStreetMap contributors
timestamp;value
2024-01-01T00:00:00Z;3
2025-01-01T00:00:00Z;3
2025-12-31T00:00:00Z;4
"""
    )


@pytest.mark.skip(reason="Failing due no fix output order!")
def test_features_group_by_tag_as_csv(
    client: TestClient, aoi_heigit: dict, expected_api_version: str
):
    response: Response = client.post(
        "/stats/features/count.csv",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {
                "start": "2024-01-01",
                "end": "2025-12-31",
                "interval": "P1Y",
            },
            "aoi": aoi_heigit,
            "groupBy": {
                "type": "byTag",
                "key": "building",
            },
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    assert (
        response.text
        == f"""# apiVersion: {expected_api_version}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: \xa9 OpenStreetMap contributors
timestamp;value;tagvalue
2024-01-01T00:00:00Z;2;university
2025-01-01T00:00:00Z;2;university
2025-12-31T00:00:00Z;3;university
2024-01-01T00:00:00Z;1;greenhouse
2025-01-01T00:00:00Z;1;greenhouse
2025-12-31T00:00:00Z;1;greenhouse
2024-01-01T00:00:00Z;3;
2025-01-01T00:00:00Z;3;
2025-12-31T00:00:00Z;4;
"""
    )
