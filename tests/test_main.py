import pytest
from fastapi.testclient import TestClient
from starlette.status import HTTP_200_OK

from ohsome_api.main import app

client = TestClient(app)


@pytest.mark.usefixtures("ohsomedb_testcontainer")
def test_read_root():
    response = client.get("/")
    assert response.status_code == HTTP_200_OK
    assert response.json() == {"Hello": "Database"}
