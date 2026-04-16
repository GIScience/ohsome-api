import pytest
from fastapi.testclient import TestClient
from starlette.status import (
    HTTP_200_OK,
    HTTP_422_UNPROCESSABLE_CONTENT,
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
    response = client.post(
        "/contributions/count.json",
        json={"filter": "building=* and building!=no and type:way"},
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"] == 3340


def test_contributions_count_as_csv():
    response = client.post(
        "/contributions/count.csv",
        json={"filter": "building=* and building!=no and type:way"},
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    expected_result = """# apiVersion: 0.0.0
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
result
3340
"""
    assert response.text == expected_result


def test_contributions_count_with_invalid_filter():
    response = client.post(
        "/contributions/count.json",
        json={"filter": "foo"},
    )
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT
    # TODO: Check error message and make it user friendly
