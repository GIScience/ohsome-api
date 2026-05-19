from datetime import datetime, timedelta, timezone

import pytest
from pydantic import ValidationError

from ohsome_api.api import Time


def test_time_start_end():
    Time(
        start=datetime(2024, 1, 1),  # no timezone
        end=datetime(2024, 3, 1),
    )


def test_time_start_end_invalid():
    with pytest.raises(ValidationError):
        Time(
            start=datetime(2024, 3, 1),  # after end
            end=datetime(2024, 1, 1),  # before start
        )


def test_time_start_end_future():
    # NOTE: valid but could be restricted/unwanted
    Time(
        start=datetime(2024, 3, 1),
        end=datetime.now() + timedelta(days=30),  # future
    )


def test_time_start_equals_end():
    with pytest.raises(ValidationError):
        Time(
            start=datetime(2024, 1, 1),
            end=datetime(2024, 1, 1),
        )


def test_time_start_before_osm():
    # NOTE: valid but could be restricted/unwanted
    # Earliest OSM timestamp is 2007-10-08T00:00:00Z"
    with pytest.raises(ValidationError):
        Time(
            start=datetime(1998, 10, 8),  # before OSM
            end=datetime(2024, 1, 1),
        )


def test_time_start_end_with_explicit_timezone():
    """Allow only UTC."""
    Time(
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end=datetime(2024, 3, 1, tzinfo=timezone.utc),
    )


def test_time_start_end_with_mixed_explicit_implicit_timezone():
    time = Time(
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end=datetime(2024, 3, 1),
    )
    assert time.start == datetime(2024, 1, 1, tzinfo=timezone.utc)
    assert time.end == datetime(2024, 3, 1, tzinfo=timezone.utc)


def test_time_start_end_with_implicit_timezone():
    """Allow only UTC."""
    # implicitly (without timezone)
    Time(start=datetime(2024, 1, 1), end=datetime(2025, 1, 1))


def test_time_start_end_timezone_invalid():
    with pytest.raises(ValidationError):
        Time(
            start=datetime(2024, 1, 1, tzinfo=timezone(timedelta(hours=2))),
            end=datetime(2024, 3, 1, tzinfo=timezone(timedelta(hours=2))),
        )


async def test_time_period():
    time = Time(
        start=datetime(2024, 1, 1),
        end=datetime(2024, 3, 1),
        period="P1M",
    )
    # validation happens via timedelta datatype,
    # but string representation is kept
    assert time.period == "P1M"


async def test_time_period_invalid():
    with pytest.raises(ValidationError):
        Time(
            start=datetime(2024, 1, 1),
            end=datetime(2024, 3, 1),
            period="P1",
        )
