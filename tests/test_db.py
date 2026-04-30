import pytest

from ohsome_api.db import get_latest_timestamp

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_latest_timestamp():
    latest_timestamp = await get_latest_timestamp()
    assert latest_timestamp.isoformat() == "2026-02-27T10:22:37+00:00"
