import pytest
from fastapi.testclient import TestClient

from ohsome_api.api import app


@pytest.fixture(scope="module")
def client(ohsomedb_testcontainer: None):
    with TestClient(app, headers={"authorization": "test"}) as client:
        yield client
