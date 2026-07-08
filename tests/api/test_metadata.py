from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK

from ohsome_api.api import VERSION


def test_metadata(client: TestClient):
    response = client.get("/metadata")
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json() == {
        "apiVersion": VERSION,
        "attribution": {
            "url": "https://ohsome.org/copyrights",
            "text": "© OpenStreetMap contributors",
        },
        "temporalExtent": {
            "earliestTimestamp": "2007-10-08T00:00:00Z",
            "latestTimestamp": "2026-05-08T20:20:44Z",
        },
    }
