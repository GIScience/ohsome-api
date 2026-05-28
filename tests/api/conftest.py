from importlib.metadata import version

import pytest
from fastapi.testclient import TestClient

from ohsome_api.api import app

VERSION = version("ohsome-api")


@pytest.fixture(scope="module")
def client(ohsomedb_testcontainer: None):
    with TestClient(app) as client:
        yield client
