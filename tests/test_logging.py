import logging

import pytest

from ohsome_api import main


@pytest.mark.asyncio
@pytest.mark.usefixtures("ohsomedb_testcontainer")
async def test_logging(caplog: pytest.LogCaptureFixture):
    caplog.set_level(logging.INFO)
    await main.get_metadata()
    assert "Get metadata from ohsomedb." in caplog.text
