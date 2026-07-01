from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK, HTTP_422_UNPROCESSABLE_CONTENT


def test_filter(client: TestClient):
    response = client.get("/filter/validation", params={"filter": "building=yes"})
    assert response.status_code == HTTP_200_OK
    assert response.json() == {"filter": "building=yes"}


def test_filter_invalid(client: TestClient):
    response = client.get("/filter/validation", params={"filter": "building==yes"})
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT
