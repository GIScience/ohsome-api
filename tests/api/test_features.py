from fastapi.testclient import TestClient
from httpx import Response
from starlette.status import HTTP_200_OK


def test_features_as_json(client: TestClient, aoi_heigit: dict):
    response = client.post(
        "/stats/features/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeSeries": {
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


def test_features_as_csv(
    client: TestClient, aoi_heigit: dict, expected_api_version: str
):
    response: Response = client.post(
        "/stats/features/count.csv",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeSeries": {
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
