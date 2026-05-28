import logging
from datetime import datetime

import pytest

from ohsome_api import api


@pytest.fixture
def mock_get_latest_timestamp(monkeypatch: pytest.MonkeyPatch):
    async def mock() -> datetime:
        return datetime.now()

    monkeypatch.setattr(
        "ohsome_api.api.service.get_latest_timestamp",
        mock,
    )
    yield


def test_log_format(caplog: pytest.LogCaptureFixture):
    with caplog.at_level(logging.INFO, logger="ohsome-api"):
        logging.getLogger("ohsome-api").info("Test message")

    output = logging.getLogger("ohsome-api").handlers[0].format(caplog.records[0])
    output_without_timestamp = " - ".join(output.split(" - ")[1:])
    assert (
        output_without_timestamp
        == "ohsome-api - INFO - test_logging.py - test_log_format - Test message"
    )


@pytest.mark.usefixtures("mock_get_latest_timestamp")
async def test_logging_info(caplog: pytest.LogCaptureFixture):
    with caplog.at_level(logging.INFO, logger="ohsome-api"):
        await api.get_metadata()
        assert "Get metadata from ohsomedb." in caplog.text


@pytest.mark.usefixtures("mock_get_latest_timestamp")
async def test_logging_error(caplog: pytest.LogCaptureFixture):
    with caplog.at_level(logging.ERROR, logger="ohsome-api"):
        await api.get_metadata()
        assert "Get metadata from ohsomedb." not in caplog.text
