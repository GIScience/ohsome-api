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
