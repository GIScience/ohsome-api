import pytest

from ohsome_api.db.metadata.metadata import get_metadata

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_get_metadata():
    metadata = await get_metadata()
    assert metadata["start"].isoformat() == "2007-10-08T00:00:00+00:00"
    assert metadata["end"].isoformat() == "2026-05-08T20:20:44+00:00"
