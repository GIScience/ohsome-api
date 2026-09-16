from datetime import datetime

from asyncpg import Record

from ohsome_api.config import CONFIG
from ohsome_api.db.db import db
from ohsome_api.db.errors import StartGreaterThanEndError, TimeSeriesTooLargeError
from ohsome_api.models import TimeBinsResult


async def generate_timestamp_series(
    start: datetime,
    end: datetime,
    interval: str | None,
) -> list[datetime]:
    if start > end:
        raise StartGreaterThanEndError(
            "Start timestamp must be smaller than end timestamp."
        )

    if start == end:
        return [start]

    if interval is None:
        return [start, end]

    limit = CONFIG.time_series_size_limit

    sql = """
        SELECT generate_series(
            $1::timestamptz,
            $2::timestamptz,
            ($3::text)::interval
        ) as ts
        LIMIT $4
    """
    records = await db.fetch_rows(sql, start, end, interval, limit + 1)

    if len(records) > limit:
        raise TimeSeriesTooLargeError(
            "The provided values for the time parameter (time bin or time series) "
            f"lead to a time series larger than {limit} points/bins."
        )

    # TODO: Extract post-processing to own function and write unit-tests
    results = [r["ts"] for r in records]
    if results[-1] != end:
        # include uneven time bin
        results.append(end)
    return results


async def get_latest_timestamp() -> datetime:
    sql = "SELECT last_timestamp FROM contributions_state"
    return (await db.fetch_row(sql))[0]


def zerofill_records_to_time_bin_columns(
    records: list[Record],
    series: list[datetime],
) -> TimeBinsResult:
    zerofilled_series = {i: 0 for i in range(len(series) - 1)}

    for record in records:
        zerofilled_series[record["time_bin"] - 1] = record["value"]

    start_timestamps: list[datetime] = [
        series[time_bin] for time_bin in zerofilled_series
    ]

    end_timestamps: list[datetime] = [
        series[time_bin + 1] for time_bin in zerofilled_series
    ]

    values: list[int] = list(zerofilled_series.values())

    return TimeBinsResult(start=start_timestamps, end=end_timestamps, value=values)
