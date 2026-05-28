from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK

from ohsome_api.api import VERSION


def test_metadata(client: TestClient):
    response = client.get("/metadata")
    assert response.status_code == HTTP_200_OK
    assert response.json() == {
        "latestTimestamp": "2026-02-27T10:22:37+00:00",
        "apiVersion": VERSION,
        "attribution": {
            "url": "https://ohsome.org/copyrights",
            "text": "© OpenStreetMap contributors",
        },
    }
