import json
from datetime import datetime, timedelta, timezone

import pytest
from pydantic import TypeAdapter, ValidationError

from ohsome_api.request_models.time import (
    TimeBins,
    TimeRange,
    TimeSeries,
    Timestamp,
)

timestamp_adapter = TypeAdapter(Timestamp)


@pytest.fixture(
    scope="module",
    params=[
        datetime(2007, 10, 8),
        datetime(2007, 10, 8, tzinfo=timezone.utc),
        "earliest",
    ],
)
def start(request: pytest.FixtureRequest):
    yield request.param


@pytest.fixture(
    scope="module",
    params=[
        datetime(2024, 3, 1),
        datetime(2024, 3, 1, tzinfo=timezone.utc),
        "latest",
    ],
)
def end(request: pytest.FixtureRequest):
    yield request.param


@pytest.fixture(
    scope="module",
    params=[
        "P1M",
        "P40D",
        "P1Y1D",
        "P3DT4H59M",
        None,
    ],
)
def duration(request: pytest.FixtureRequest):
    yield request.param


def test_timestamp_utc_is_preserved():
    timestamp = datetime(2024, 1, 1, tzinfo=timezone.utc)
    result = timestamp_adapter.validate_python(timestamp)
    assert result.tzinfo == timezone.utc


def test_timestamp_naive_becomes_utc():
    result = timestamp_adapter.validate_python(datetime(2024, 1, 1))
    assert result == datetime(2024, 1, 1, tzinfo=timezone.utc)


def test_timestamp_non_utc_offset_is_rejected():
    timestamp = datetime(2024, 1, 1, tzinfo=timezone(timedelta(hours=2)))
    with pytest.raises(ValidationError, match="Only UTC"):
        timestamp_adapter.validate_python(timestamp)


def test_timestamp_before_osm_epoch_rejected():
    with pytest.raises(ValidationError, match="2007-10-08"):
        timestamp_adapter.validate_python(datetime(1998, 10, 8, tzinfo=timezone.utc))


def test_timestamp_at_osm_epoch_ok():
    timestamp_adapter.validate_python(datetime(2007, 10, 8, tzinfo=timezone.utc))


def test_timestamp_future():
    timestamp_adapter.validate_python(datetime.now() + timedelta(days=30))  # future


def test_time_timerange_valid(start: datetime | str, end: datetime | str):
    time_range = TimeRange(start=start, end=end)
    assert time_range.start == datetime(2007, 10, 8, tzinfo=timezone.utc)  # property
    assert time_range.end in (datetime(2024, 3, 1, tzinfo=timezone.utc), "latest")


@pytest.mark.parametrize(
    "start",
    (
        datetime(2024, 3, 1),
        datetime(2024, 1, 1),
    ),
)
def test_time_timerange_start_greater_or_equal_than_end(start: datetime | str):
    with pytest.raises(
        ValidationError,
        match="End timestamp needs to be greater than start timestamp",
    ):
        TimeRange(
            start=start,
            end=datetime(2024, 1, 1),
        )


def test_time_bin_valid(
    start: datetime | str,
    end: datetime | str,
    duration: str | None,
):
    time_bins = TimeBins(start=start, end=end, bin_size=duration)
    assert time_bins.start == datetime(2007, 10, 8, tzinfo=timezone.utc)  # property
    assert time_bins.end in (
        datetime(2024, 3, 1, tzinfo=timezone.utc),
        "latest",
    )
    assert time_bins.bin_size == duration  # bin size is validated but stays a string


async def test_time_bin_from_json():
    TimeBins.model_validate_json(
        json.dumps(
            {
                "start": "2025-01-01",
                "end": "2025-12-31T00:00Z",
            }
        )
    )


async def test_bin_size_invalid_iso_interval():
    with pytest.raises(ValidationError):
        TimeBins(
            start=datetime(2024, 1, 1),
            end=datetime(2024, 3, 1),
            bin_size="P1",
        )


def test_time_series_valid(
    start: datetime | str,
    end: datetime | str,
    duration: str | None,
):
    time_interval = TimeSeries(start=start, end=end, interval=duration)
    assert time_interval.start == datetime(2007, 10, 8, tzinfo=timezone.utc)  # property
    assert time_interval.end in (
        datetime(2024, 3, 1, tzinfo=timezone.utc),
        "latest",
    )
    # bin size is validated but stays a string
    assert time_interval.interval == duration
