import pytest
from ohsome_filter_to_sql import ohsome_filter_to_sql

from ohsome_api.db import extract_features, get_metadata

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_get_metadata():
    metadata = await get_metadata()
    assert metadata["start"].isoformat() == "2007-10-08T00:00:00+00:00"
    assert metadata["end"].isoformat() == "2026-05-08T20:20:44+00:00"


async def test_extract_features(aoi_wkt_audimax: str):
    ohsome_filter = "id:node/1702635807"
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    producer = extract_features(
        query_where_clause,
        query_args,
        aoi_wkt_audimax,
        clip=True,
        start="latest",
        end="latest",
        contributions=False,
    )
    async for batch in producer:
        assert len(batch) == 1
        assert batch[0]["osm_type"] == "node"
        assert batch[0]["osm_id"] == 1702635807
        assert isinstance(batch[0]["tags"], dict)
