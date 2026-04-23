import logging

import pytest

from ohsome_api import api


@pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")
async def test_logging(caplog: pytest.LogCaptureFixture):
    caplog.set_level(logging.INFO)
    await api.get_metadata()
    assert "Get metadata from ohsomedb." in caplog.text
