import pytest
from fastapi.testclient import TestClient
from starlette.status import (
    HTTP_200_OK,
    HTTP_404_NOT_FOUND,
    HTTP_422_UNPROCESSABLE_CONTENT,
)

from ohsome_api.api import app

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


def test_contributions_count_as_json():
    response = client.post(
        "/contributions/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {"start": "2025-01-01", "end": "2025-12-31"},
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"][0]["value"] == 131
    assert len(response.json()["result"]) == 1


# TODO: parametrize period
def test_contributions_count_as_json_time_period():
    response = client.post(
        "/contributions/count.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {"start": "2025-01-01", "end": "2025-12-31", "period": "P1M"},
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/json"
    assert response.json()["result"][0]["value"] == 131
    assert len(response.json()["result"]) == 1


def test_contributions_count_as_csv():
    response = client.post(
        "/contributions/count.csv",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {"start": "2025-01-01", "end": "2025-12-31"},
        },
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "text/csv; charset=utf-8"
    expected_result = """# apiVersion: 0.0.0
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start,end,value
2025-01-01T00:00:00,2025-12-31T00:00:00,131
"""
    # TODO: Why no timezone in response?
    assert response.text == expected_result


def test_contributions_count_with_invalid_filter():
    response = client.post(
        "/contributions/count.json",
        json={"filter": "foo", "time": {"start": "2025-01-01", "end": "2025-12-31"}},
    )
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT
    # TODO: Check error message and make it user friendly


@pytest.mark.parametrize(
    "case",
    [
        "2025-02-31",  # invalid date
        "",  # empty
        "foo",  # garbage
    ],
)
def test_contributions_count_with_invalid_time(case: str):
    response = client.post(
        "/contributions/count.json",
        json={"filter": "id:1", "time": {"start": case, "end": "2025-12-31"}},
    )
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT


def test_contributions_count_without_format():
    response = client.post(
        "/contributions/count",
        json={"filter": "id:1", "time": {"start": "2025-01-01", "end": "2025-12-31"}},
    )
    assert response.status_code == HTTP_404_NOT_FOUND
