from datetime import datetime, timedelta, timezone

import pytest

from ohsome_api.models import MeasureEnum, TimeBinRow
from ohsome_api.service import get_currentness_row

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_get_currentness_count(aoi_wkt_heigit: str):
    start = datetime(year=2025, month=1, day=1)
    end = start + timedelta(days=365)
    result = await get_currentness_row(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size=None,
        aoi_wkt=aoi_wkt_heigit,
        measure=MeasureEnum.COUNT,
        clip=True,
    )
    assert result == [
        TimeBinRow(
            start=start,
            end=end,
            value=2,
        )
    ]


async def test_get_currentness_count_latest(aoi_wkt_heigit: str):
    start = datetime(year=2025, month=1, day=1)
    end = "latest"
    result = await get_currentness_row(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size=None,
        aoi_wkt=aoi_wkt_heigit,
        measure=MeasureEnum.COUNT,
        clip=True,
    )
    assert result == [
        TimeBinRow(
            start=start,
            end=datetime(2026, 5, 8, 20, 20, 44, tzinfo=timezone.utc),
            value=4,
        )
    ]


async def test_get_currentness_count_with_bin_size(aoi_wkt_heigit: str):
    start = datetime(year=2025, month=7, day=1, tzinfo=timezone.utc)
    end = datetime(year=2025, month=10, day=1, tzinfo=timezone.utc)
    result = await get_currentness_row(
        "building=* and building!=no and type:way",
        start,
        end,
        "P1M",
        aoi_wkt_heigit,
        measure=MeasureEnum.COUNT,
        clip=True,
    )

    assert result == [
        TimeBinRow(
            start=datetime(year=2025, month=7, day=1, tzinfo=timezone.utc),
            end=datetime(year=2025, month=8, day=1, tzinfo=timezone.utc),
            value=1,
        ),
        TimeBinRow(
            start=datetime(year=2025, month=8, day=1, tzinfo=timezone.utc),
            end=datetime(year=2025, month=9, day=1, tzinfo=timezone.utc),
            value=0,  # NOTE: zero filled value
        ),
        TimeBinRow(
            start=datetime(year=2025, month=9, day=1, tzinfo=timezone.utc),
            end=datetime(year=2025, month=10, day=1, tzinfo=timezone.utc),
            value=0,  # NOTE: zero filled value
        ),
    ]


async def test_get_currentness_count_by_month(aoi_wkt_heigit: str):
    start = datetime(year=2022, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365)
    bin_2022 = await get_currentness_row(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size=None,
        aoi_wkt=aoi_wkt_heigit,
        measure=MeasureEnum.COUNT,
        clip=True,
    )

    start = datetime(year=2023, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365)
    bin_2023 = await get_currentness_row(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size=None,
        aoi_wkt=aoi_wkt_heigit,
        measure=MeasureEnum.COUNT,
        clip=True,
    )

    start = datetime(year=2022, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365 * 2)
    bin_size = "P1Y"
    bins = await get_currentness_row(
        "building=* and building!=no and type:way",
        start,
        end,
        bin_size,
        aoi_wkt_heigit,
        measure=MeasureEnum.COUNT,
        clip=True,
    )
    assert bins == [
        TimeBinRow(
            start=start,
            end=datetime(year=2023, month=1, day=1, tzinfo=timezone.utc),
            value=bin_2022[0].value,
        ),
        TimeBinRow(
            start=datetime(year=2023, month=1, day=1, tzinfo=timezone.utc),
            end=end,
            value=bin_2023[0].value,
        ),
    ]
