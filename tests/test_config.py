from pytest import MonkeyPatch

from ohsome_api.config import Config


def test_config(monkeypatch: MonkeyPatch):
    monkeypatch.setenv("OHSOME_API__OHSOMEDB__TIMEOUT_STATS", "1")
    config = Config()
    assert config.ohsomedb.timeout_stats == 1
