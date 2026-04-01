import pytest
from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK

from ohsome_api.main import app

client = TestClient(app)


@pytest.mark.usefixtures("ohsomedb_testcontainer")
def test_metadata():
    response = client.get("/metadata")
    assert response.status_code == HTTP_200_OK
    assert response.json() == {
        "latestTimestamp": "2026-02-27T10:22:37+00:00",
        "apiVersion": "0.0.0",
    }
