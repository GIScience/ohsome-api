import logging

import pytest

from ohsome_api import main


@pytest.mark.asyncio
async def test_logging(caplog: pytest.LogCaptureFixture):
    caplog.set_level(logging.INFO)
    await main.read_root()
    assert "hello world request" in caplog.text
