from importlib.metadata import version

from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK


def test_activity_users_as_json(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/activity/users.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert len(response.json()["result"]) == 1
    assert response.json()["result"][0]["value"] == 6


def test_activity_users_as_csv(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/activity/users.csv",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeBins": {"start": "2024-01-01", "end": "2025-12-31"},
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    expected_result = f"""# apiVersion: {version("ohsome-api")}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start;end;value
2024-01-01T00:00:00Z;2025-12-31T00:00:00Z;6
"""
    assert response.text == expected_result
