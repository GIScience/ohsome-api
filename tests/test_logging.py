import logging

import pytest


def test_log_format(caplog: pytest.LogCaptureFixture):
    with caplog.at_level(logging.INFO):
        logging.info("Test message")

    output = logging.getLogger("root").handlers[0].format(caplog.records[0])
    output_without_timestamp = " - ".join(output.split(" - ")[1:])
    assert (
        output_without_timestamp
        == "root - INFO - test_logging.py - test_log_format - Test message"
    )
