import pytest
from ohsome_filter_to_sql import ohsome_filter_to_sql

from ohsome_api.db import extract_features, get_latest_timestamp

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_latest_timestamp():
    latest_timestamp = await get_latest_timestamp()
    assert latest_timestamp.isoformat() == "2026-02-27T10:22:37+00:00"


async def test_extract_features(aoi_wkt_audimax: str):
    ohsome_filter = "id:node/1702635807"
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    producer = extract_features(query_where_clause, query_args, aoi_wkt_audimax)
    async for batch in producer:
        assert len(batch) == 1
        assert batch[0]["osm_type"] == "node"
        assert batch[0]["osm_id"] == 1702635807
        assert isinstance(batch[0]["tags"], dict)
