from datetime import datetime
from pathlib import Path

from ohsome_api.config import CONFIG
from ohsome_api.database import db

QUERIES_DIR = Path(__file__).parent / "queries"


class TimeSeriesTooLargeError(ValueError):
    pass


class ResultTooLargeError(ValueError):
    pass


async def generate_timestamp_series(
    start: datetime,
    end: datetime,
    interval: str | None,
) -> list[datetime]:
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
