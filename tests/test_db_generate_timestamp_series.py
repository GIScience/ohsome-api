from datetime import datetime, timezone

import pytest

from ohsome_api.db import generate_timestamp_series

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer", "database_pool")]


async def test_generate_timestamp_series_single_time_bin():
    start = datetime(2026, 11, 1, tzinfo=timezone.utc)
    end_one_month_later = datetime(2026, 12, 1, tzinfo=timezone.utc)
    series = await generate_timestamp_series(start, end_one_month_later, None)
    assert series == [start, end_one_month_later]


async def test_generate_timestamp_series_monthly_1_month():
    start = datetime(2026, 11, 1, tzinfo=timezone.utc)
    end_one_month_later = datetime(2026, 12, 1, tzinfo=timezone.utc)
    bucket_size = "P1M"
    series = await generate_timestamp_series(start, end_one_month_later, bucket_size)
    assert series == [start, end_one_month_later]


async def test_generate_timestamp_series_monthly_half_month():
    start = datetime(2026, 11, 1, tzinfo=timezone.utc)
    end_6_weeks_later = datetime(2026, 12, 15, tzinfo=timezone.utc)
    bucket_size = "P1M"
    series = await generate_timestamp_series(start, end_6_weeks_later, bucket_size)
    assert series == [
        start,
        datetime(2026, 12, 1, tzinfo=timezone.utc),
        end_6_weeks_later,
    ]


async def test_generate_timestamp_series_monthly_12_month():
    start = datetime(2025, 1, 1, tzinfo=timezone.utc)
    end_one_year_later = datetime(2026, 1, 1, tzinfo=timezone.utc)
    bucket_size = "P1M"
    series = await generate_timestamp_series(start, end_one_year_later, bucket_size)
    assert len(series) == 13
    assert series == [
        start,
        datetime(2025, 2, 1, tzinfo=timezone.utc),
        datetime(2025, 3, 1, tzinfo=timezone.utc),
        datetime(2025, 4, 1, tzinfo=timezone.utc),
        datetime(2025, 5, 1, tzinfo=timezone.utc),
        datetime(2025, 6, 1, tzinfo=timezone.utc),
        datetime(2025, 7, 1, tzinfo=timezone.utc),
        datetime(2025, 8, 1, tzinfo=timezone.utc),
        datetime(2025, 9, 1, tzinfo=timezone.utc),
        datetime(2025, 10, 1, tzinfo=timezone.utc),
        datetime(2025, 11, 1, tzinfo=timezone.utc),
        datetime(2025, 12, 1, tzinfo=timezone.utc),
        end_one_year_later,
    ]


async def test_generate_timestamp_series_exceeds_limit():
    start = datetime(2023, 11, 1, tzinfo=timezone.utc)
    end_one_month_later = datetime(2026, 12, 1, tzinfo=timezone.utc)
    bucket_size = "PT1S"
    with pytest.raises(ValueError):
        await generate_timestamp_series(start, end_one_month_later, bucket_size)
