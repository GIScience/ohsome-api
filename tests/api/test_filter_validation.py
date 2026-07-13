from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK, HTTP_422_UNPROCESSABLE_CONTENT


def test_filter_get(client: TestClient, expected_api_version: str):
    response = client.get("/filter/validation", params={"filter": "building=yes"})
    assert response.status_code == HTTP_200_OK
    assert response.json() == {
        "apiVersion": expected_api_version,
        "attribution": {
            "text": "© OpenStreetMap contributors",
            "url": "https://ohsome.org/copyrights",
        },
        "filter": "building=yes",
    }


def test_filter_post(client: TestClient, expected_api_version: str):
    response = client.post("/filter/validation", json={"filter": "building=yes"})
    assert response.status_code == HTTP_200_OK
    assert response.json() == {
        "apiVersion": expected_api_version,
        "attribution": {
            "text": "© OpenStreetMap contributors",
            "url": "https://ohsome.org/copyrights",
        },
        "filter": "building=yes",
    }


def test_filter_invalid(client: TestClient):
    response = client.get("/filter/validation", params={"filter": "building==yes"})
    assert response.status_code == HTTP_422_UNPROCESSABLE_CONTENT
    assert response.json() == {
        "detail": [
            {
                "type": "value_error",
                "loc": [
                    "query",
                    "filter",
                ],
                "msg": "Value error, line 1:9 no viable alternative at input 'building=='",  # noqa: E501
                "input": "building==yes",
                "ctx": {
                    "error": {},
                },
            },
        ],
    }
