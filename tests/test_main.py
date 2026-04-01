import pytest
from fastapi.testclient import TestClient
from starlette.status import (
    HTTP_200_OK,
)

from ohsome_api.main import app

client = TestClient(app)

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer")]


def test_metadata():
    response = client.get("/metadata")
    assert response.status_code == HTTP_200_OK
    assert response.json() == {
        "latestTimestamp": "2026-02-27T10:22:37+00:00",
        "apiVersion": "0.0.0",
        "attribution": {
            "url": "https://ohsome.org/copyrights",
            "text": "© OpenStreetMap contributors",
        },
    }


def test_contributions_count():
    response = client.get("/contributions/count.json")
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"] == 44009


def test_contributions_count_as_csv():
    response = client.get("/contributions/count.csv")
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    assert "foo" in response.text
