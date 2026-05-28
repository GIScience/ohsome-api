import logging

import pytest

from ohsome_api import api


@pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")
async def test_logging_info(caplog: pytest.LogCaptureFixture):
    caplog.set_level(logging.INFO)
    await api.get_metadata()
    assert "Get metadata from ohsomedb." in caplog.text


@pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")
async def test_logging_error(caplog: pytest.LogCaptureFixture):
    caplog.set_level(logging.ERROR)
    await api.get_metadata()
    assert "Get metadata from ohsomedb." not in caplog.text
