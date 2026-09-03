import pytest

from ohsome_api.db.extraction.features import extract_features

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_extract_features(aoi_wkt_audimax: str):
    ohsome_filter = "id:node/1702635807"
    producer = extract_features(
        ohsome_filter,
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
