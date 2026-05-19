from datetime import datetime, timedelta, timezone

import pytest

from ohsome_api.models import RowModel
from ohsome_api.service import get_contributions_count

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_get_contributions_count():
    start = datetime(year=2022, month=1, day=1)
    end = start + timedelta(days=365)
    result = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        bucket_size=None,
    )
    assert result == [
        RowModel(
            start_timestamp=start,
            end_timestamp=end,
            value=122,
        )
    ]


async def test_get_contributions_count_with_bucket_size():
    start = datetime(year=2022, month=7, day=1, tzinfo=timezone.utc)
    end = datetime(year=2022, month=10, day=1, tzinfo=timezone.utc)
    result = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        "P1M",
    )

    assert result == [
        RowModel(
            start_timestamp=datetime(year=2022, month=7, day=1, tzinfo=timezone.utc),
            end_timestamp=datetime(year=2022, month=8, day=1, tzinfo=timezone.utc),
            value=29,
        ),
        RowModel(
            start_timestamp=datetime(year=2022, month=8, day=1, tzinfo=timezone.utc),
            end_timestamp=datetime(year=2022, month=9, day=1, tzinfo=timezone.utc),
            value=0,  # NOTE: zero filled value
        ),
        RowModel(
            start_timestamp=datetime(year=2022, month=9, day=1, tzinfo=timezone.utc),
            end_timestamp=datetime(year=2022, month=10, day=1, tzinfo=timezone.utc),
            value=3,
        ),
    ]


async def test_get_contributions_count_by_month():
    start = datetime(year=2022, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365)
    bin_2022 = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        bucket_size=None,
    )

    start = datetime(year=2023, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365)
    bin_2023 = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        bucket_size=None,
    )

    start = datetime(year=2022, month=1, day=1, tzinfo=timezone.utc)
    end = start + timedelta(days=365 * 2)
    bucket_size = "P1Y"
    bins = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        bucket_size,
    )
    assert bins == [
        RowModel(
            start_timestamp=start,
            end_timestamp=datetime(year=2023, month=1, day=1, tzinfo=timezone.utc),
            value=bin_2022[0].value,
        ),
        RowModel(
            start_timestamp=datetime(year=2023, month=1, day=1, tzinfo=timezone.utc),
            end_timestamp=end,
            value=bin_2023[0].value,
        ),
    ]
