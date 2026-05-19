from datetime import datetime, timedelta, timezone

import pytest
from pydantic import ValidationError

from ohsome_api.request_models import Time


def test_time_start_end():
    Time(
        start_timestamp=datetime(2024, 1, 1),  # no timezone
        end_timestamp=datetime(2024, 3, 1),
    )


def test_time_start_end_invalid():
    with pytest.raises(ValidationError):
        Time(
            start_timestamp=datetime(2024, 3, 1),  # after end
            end_timestamp=datetime(2024, 1, 1),  # before start
        )


def test_time_start_end_future():
    Time(
        start_timestamp=datetime(2024, 3, 1),
        end_timestamp=datetime.now() + timedelta(days=30),  # future
    )


def test_time_start_equals_end():
    with pytest.raises(ValidationError):
        Time(
            start_timestamp=datetime(2024, 1, 1),
            end_timestamp=datetime(2024, 1, 1),
        )


def test_time_start_before_osm():
    # NOTE: valid but could be restricted/unwanted
    # Earliest OSM timestamp is 2007-10-08T00:00:00Z"
    with pytest.raises(ValidationError):
        Time(
            start_timestamp=datetime(1998, 10, 8),  # before OSM
            end_timestamp=datetime(2024, 1, 1),
        )


def test_time_start_end_with_explicit_timezone():
    """Allow only UTC."""
    Time(
        start_timestamp=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end_timestamp=datetime(2024, 3, 1, tzinfo=timezone.utc),
    )


def test_time_start_end_with_mixed_explicit_implicit_timezone():
    time = Time(
        start_timestamp=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end_timestamp=datetime(2024, 3, 1),
    )
    assert time.start_timestamp == datetime(2024, 1, 1, tzinfo=timezone.utc)
    assert time.end_timestamp == datetime(2024, 3, 1, tzinfo=timezone.utc)


def test_time_start_end_with_implicit_timezone():
    """Allow only UTC."""
    # implicitly (without timezone)
    Time(start_timestamp=datetime(2024, 1, 1), end_timestamp=datetime(2025, 1, 1))


def test_time_start_end_timezone_invalid():
    with pytest.raises(ValidationError):
        Time(
            start_timestamp=datetime(2024, 1, 1, tzinfo=timezone(timedelta(hours=2))),
            end_timestamp=datetime(2024, 3, 1, tzinfo=timezone(timedelta(hours=2))),
        )


async def test_time_bucket_size():
    time = Time(
        start_timestamp=datetime(2024, 1, 1),
        end_timestamp=datetime(2024, 3, 1),
        bucket_size="P1M",
    )
    # validation happens via timedelta datatype,
    # but string representation is kept
    assert time.bucket_size == "P1M"


async def test_time_bucket_size_invalid():
    with pytest.raises(ValidationError):
        Time(
            start_timestamp=datetime(2024, 1, 1),
            end_timestamp=datetime(2024, 3, 1),
            bucket_size="P1",
        )
