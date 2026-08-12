import json
from datetime import datetime, timedelta, timezone

import pytest
from pydantic import ValidationError

from ohsome_api.request_models.time import TimeBinSizeRequestModel


def test_start_end():
    time_bins = TimeBinSizeRequestModel(
        start=datetime(2024, 1, 1),  # no timezone
        end=datetime(2024, 3, 1),
    )
    assert time_bins.start == datetime(2024, 1, 1, tzinfo=timezone.utc)
    assert time_bins.end == datetime(2024, 3, 1, tzinfo=timezone.utc)


async def test_start_end_from_json():
    TimeBinSizeRequestModel.model_validate_json(
        json.dumps(
            {
                "start": "2025-01-01",
                "end": "2025-12-31T00:00Z",
            }
        )
    )


def test_start_end_earliest():
    time_bins = TimeBinSizeRequestModel(
        start="earliest",
        end=datetime(2024, 3, 1),
    )
    assert time_bins.start == datetime(2007, 10, 8, tzinfo=timezone.utc)


def test_end_after_start():
    with pytest.raises(ValidationError):
        TimeBinSizeRequestModel(
            start=datetime(2024, 3, 1),  # after end
            end=datetime(2024, 1, 1),  # before start
        )


def test_end_in_future():
    TimeBinSizeRequestModel(
        start=datetime(2024, 3, 1),
        end=datetime.now() + timedelta(days=30),  # future
    )


def test_start_equals_end():
    with pytest.raises(ValidationError):
        TimeBinSizeRequestModel(
            start=datetime(2024, 1, 1),
            end=datetime(2024, 1, 1),
        )


def test_start_before_osm():
    with pytest.raises(ValidationError):
        TimeBinSizeRequestModel(
            start=datetime(1998, 10, 8),  # before OSM
            end=datetime(2024, 1, 1),
        )


@pytest.mark.parametrize(
    "start",
    [
        "2025-31-45",  # invalid date
        "",  # empty
        "foo",  # garbage
    ],
)
def test_start_invalid(start: str):
    with pytest.raises(ValidationError):
        TimeBinSizeRequestModel(
            start=start,
            end=datetime(2024, 1, 1),
        )


def test_start_end_with_explicit_timezone():
    """Allow only UTC."""
    TimeBinSizeRequestModel(
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end=datetime(2024, 3, 1, tzinfo=timezone.utc),
    )


def test_start_end_with_mixed_explicit_implicit_timezone():
    time = TimeBinSizeRequestModel(
        start=datetime(2024, 1, 1, tzinfo=timezone.utc),
        end=datetime(2024, 3, 1),
    )
    assert time.start == datetime(2024, 1, 1, tzinfo=timezone.utc)
    assert time.end == datetime(2024, 3, 1, tzinfo=timezone.utc)


def test_start_end_with_implicit_timezone():
    """Allow only UTC."""
    # implicitly (without timezone)
    TimeBinSizeRequestModel(start=datetime(2024, 1, 1), end=datetime(2025, 1, 1))


def test_start_end_timezone_invalid():
    with pytest.raises(ValidationError):
        TimeBinSizeRequestModel(
            start=datetime(2024, 1, 1, tzinfo=timezone(timedelta(hours=2))),
            end=datetime(2024, 3, 1, tzinfo=timezone(timedelta(hours=2))),
        )


@pytest.mark.parametrize(
    "bin_size",
    ("P1M", "P40D", "P1Y1D", "P3DT4H59M", None),
)
async def test_bin_size(bin_size: str):
    time = TimeBinSizeRequestModel(
        start=datetime(2024, 1, 1),
        end=datetime(2024, 3, 1),
        bin_size=bin_size,
    )
    # validation happens via timedelta datatype,
    # but string representation is kept
    assert time.bin_size == bin_size


async def test_bin_size_invalid():
    with pytest.raises(ValidationError):
        TimeBinSizeRequestModel(
            start=datetime(2024, 1, 1),
            end=datetime(2024, 3, 1),
            bin_size="P1",
        )
