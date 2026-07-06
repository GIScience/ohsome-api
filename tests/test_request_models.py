from datetime import datetime, timedelta, timezone

import pytest
from pydantic import ValidationError

from ohsome_api.request_models import TimeBinsRequestModel


def test_time_start_end():
    time_bins = TimeBinsRequestModel(
        start=datetime(2024, 1, 1),  # no timezone
        end=datetime(2024, 3, 1),
    )
    assert time_bins.start == datetime(2024, 1, 1, tzinfo=timezone.utc)
    assert time_bins.end == datetime(2024, 3, 1, tzinfo=timezone.utc)


def test_time_start_end_earliest():
    time_bins = TimeBinsRequestModel(
        start="earliest",
        end=datetime(2024, 3, 1),
    )
    assert time_bins.start == datetime(2007, 10, 8, tzinfo=timezone.utc)


def test_time_start_end_invalid():
    with pytest.raises(ValidationError):
        TimeBinsRequestModel(
            start=datetime(2024, 3, 1),  # after end
            end=datetime(2024, 1, 1),  # before start
        )


def test_time_start_end_future():
    TimeBinsRequestModel(
        start=datetime(2024, 3, 1),
        end=datetime.now() + timedelta(days=30),  # future
    )


def test_time_start_equals_end():
    with pytest.raises(ValidationError):
        TimeBinsRequestModel(
            start=datetime(2024, 1, 1),
            end=datetime(2024, 1, 1),
        )


def test_time_start_before_osm():
    # NOTE: valid but could be restricted/unwanted
    # Earliest OSM timestamp is 2007-10-08T00:00:00Z"
    with pytest.raises(ValidationError):
        TimeBinsRequestModel(
            start=datetime(1998, 10, 8),  # before OSM
            end=datetime(2024, 1, 1),
        )


def test_time_start_end_with_explicit_timezone():
    """Allow only UTC."""
    TimeBinsRequestModel(
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end=datetime(2024, 3, 1, tzinfo=timezone.utc),
    )


def test_time_start_end_with_mixed_explicit_implicit_timezone():
    time = TimeBinsRequestModel(
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end=datetime(2024, 3, 1),
    )
    assert time.start == datetime(2024, 1, 1, tzinfo=timezone.utc)
    assert time.end == datetime(2024, 3, 1, tzinfo=timezone.utc)


def test_time_start_end_with_implicit_timezone():
    """Allow only UTC."""
    # implicitly (without timezone)
    TimeBinsRequestModel(start=datetime(2024, 1, 1), end=datetime(2025, 1, 1))


def test_time_start_end_timezone_invalid():
    with pytest.raises(ValidationError):
        TimeBinsRequestModel(
            start=datetime(2024, 1, 1, tzinfo=timezone(timedelta(hours=2))),
            end=datetime(2024, 3, 1, tzinfo=timezone(timedelta(hours=2))),
        )


@pytest.mark.parametrize(
    "bin_size",
    ("P1M", "P40D", "P1Y1D", "P3DT4H59M"),
)
async def test_time_bin_size(bin_size: str):
    time = TimeBinsRequestModel(
        start=datetime(2024, 1, 1),
        end=datetime(2024, 3, 1),
        bin_size=bin_size,
    )
    # validation happens via timedelta datatype,
    # but string representation is kept
    assert time.bin_size == bin_size


async def test_time_bin_size_invalid():
    with pytest.raises(ValidationError):
        TimeBinsRequestModel(
            start=datetime(2024, 1, 1),
            end=datetime(2024, 3, 1),
            bin_size="P1",
        )
