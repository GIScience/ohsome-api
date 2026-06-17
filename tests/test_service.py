from datetime import datetime, timedelta, timezone

import pytest

from ohsome_api.models import ExtractionRow, TimeBinsRowModel
from ohsome_api.request_models import Measure
from ohsome_api.service import get_currentness, get_extracted_features

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_get_contributions_count(aoi_wkt_heigit: str):
    start = datetime(year=2025, month=1, day=1)
    end = start + timedelta(days=365)
    result = await get_currentness(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size=None,
        aoi_wkt=aoi_wkt_heigit,
        measure=Measure.COUNT,
    )
    assert result == [
        TimeBinsRowModel(
            start=start,
            end=end,
            value=4,
        )
    ]


async def test_get_contributions_count_with_bin_size(aoi_wkt_heigit: str):
    start = datetime(year=2025, month=7, day=1, tzinfo=timezone.utc)
    end = datetime(year=2025, month=10, day=1, tzinfo=timezone.utc)
    result = await get_currentness(
        "building=* and building!=no and type:way",
        start,
        end,
        "P1M",
        aoi_wkt_heigit,
        measure=Measure.COUNT,
    )

    assert result == [
        TimeBinsRowModel(
            start=datetime(year=2025, month=7, day=1, tzinfo=timezone.utc),
            end=datetime(year=2025, month=8, day=1, tzinfo=timezone.utc),
            value=2,
        ),
        TimeBinsRowModel(
            start=datetime(year=2025, month=8, day=1, tzinfo=timezone.utc),
            end=datetime(year=2025, month=9, day=1, tzinfo=timezone.utc),
            value=0,  # NOTE: zero filled value
        ),
        TimeBinsRowModel(
            start=datetime(year=2025, month=9, day=1, tzinfo=timezone.utc),
            end=datetime(year=2025, month=10, day=1, tzinfo=timezone.utc),
            value=0,  # NOTE: zero filled value
        ),
    ]


async def test_get_contributions_count_by_month(aoi_wkt_heigit: str):
    start = datetime(year=2022, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365)
    bin_2022 = await get_currentness(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size=None,
        aoi_wkt=aoi_wkt_heigit,
        measure=Measure.COUNT,
    )

    start = datetime(year=2023, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365)
    bin_2023 = await get_currentness(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size=None,
        aoi_wkt=aoi_wkt_heigit,
        measure=Measure.COUNT,
    )

    start = datetime(year=2022, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365 * 2)
    bin_size = "P1Y"
    bins = await get_currentness(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size,
        aoi_wkt_heigit,
        measure=Measure.COUNT,
    )
    assert bins == [
        TimeBinsRowModel(
            start=start,
            end=datetime(year=2023, month=1, day=1, tzinfo=timezone.utc),
            value=bin_2022[0].value,
        ),
        TimeBinsRowModel(
            start=datetime(year=2023, month=1, day=1, tzinfo=timezone.utc),
            end=end,
            value=bin_2023[0].value,
        ),
    ]


class CollectingWriter:
    def __init__(self) -> None:
        self.rows: list[ExtractionRow] = []
        self.closed = False

    def write_batch(self, batch: list[ExtractionRow]) -> None:
        self.rows.extend(batch)

    def close(self) -> None:
        self.closed = True


async def test_extraction():
    writer = CollectingWriter()
    await get_extracted_features("id:way/274497164", writer)
    assert len(writer.rows) == 1
    assert writer.closed
    assert writer.rows[0]["osm_type"] == "way"
    assert writer.rows[0]["osm_id"] == 274497164
    assert all(isinstance(row["tags"], dict) for row in writer.rows)
