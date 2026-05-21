import pytest
from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK

from ohsome_api.api import app


# TODO: Factor out to common conftest.py in tests/api/conftest.py
@pytest.fixture(scope="module")
def client():
    with TestClient(app) as client:
        yield client


pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer")]


def test_features_as_json(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/features/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "timeSeries": {
                "start": "2024-01-01",
                "end": "2025-12-31",
                "interval": "P1Y",
            },
            "aoi": aoi_geojson_heigit,
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    result = response.json()["result"]
    assert result == [
        {
            "timestamp": "2024-01-01T00:00:00Z",
            "value": 3,
        },
        {
            "timestamp": "2025-01-01T00:00:00Z",
            "value": 3,
        },
        {
            "timestamp": "2025-12-31T00:00:00Z",
            "value": 4,
        },
    ]
