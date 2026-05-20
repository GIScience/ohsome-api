from datetime import datetime

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.models import RowModel

SCHEMA = CONFIG.ohsomedb.schemaname


async def generate_timestamp_series(
    start_timestamp: datetime,
    end_timestamp: datetime,
    bucket_size: str | None,
    limit: int = 10_000,
) -> list[datetime]:
    if bucket_size is None:
        return [start_timestamp, end_timestamp]

    sql = """
        SELECT generate_series(
            $1::timestamptz,
            $2::timestamptz,
            ($3::text)::interval
        ) as ts
        LIMIT $4
    """
    records = await db.fetch_rows(
        sql, start_timestamp, end_timestamp, bucket_size, limit + 1
    )

    if len(records) > limit:
        # TODO: Use custom exception and handle it in fastapi
        # TODO: Write API integration test to check if error gets to user
        # TODO: Add limitation to docs
        raise ValueError(
            "Time parameters including bucket_size lead to "
            f"a time series larger than {limit} bins."
        )

    # TODO: Extract post-processing to own function and write unit-tests
    results = [r["ts"] for r in records]
    if results[-1] != end_timestamp:
        # include uneven time bin
        results.append(end_timestamp)
    return results


async def get_latest_timestamp() -> datetime:
    sql = f'SELECT last_timestamp FROM "{SCHEMA}".contributions_state'  # noqa: S608
    record = await db.fetch_row(sql)
    if not isinstance(record["last_timestamp"], datetime):
        raise TypeError()
    return record["last_timestamp"]


async def get_contributions_count(
    filter_where_clause: str,
    filter_args: tuple,
    start_timestamp: datetime,
    end_timestamp: datetime,
    series: list[datetime],
) -> list[RowModel]:
    filter_args_count = len(filter_args)
    sql = f"""
        SELECT
            COUNT(*) AS count,
            width_bucket(valid_from, ${filter_args_count + 3}::timestamptz[]) AS time_bin
        FROM "{SCHEMA}".contributions
        WHERE ({filter_where_clause})
        AND valid_from BETWEEN ${filter_args_count + 1}::timestamptz
                           AND ${filter_args_count + 2}::timestamptz
        GROUP BY time_bin
        ORDER BY time_bin
    """  # noqa: S608, E501
    records = await db.fetch_rows(
        sql,
        *filter_args,
        start_timestamp,
        end_timestamp,
        series,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_series = {i: 0 for i in range(len(series) - 1)}

    for record in records:
        zerofilled_series[record["time_bin"] - 1] = record["count"]

    return [
        RowModel(
            value=count,
            start_timestamp=series[time_bin],
            end_timestamp=series[time_bin + 1],
        )
        for time_bin, count in zerofilled_series.items()
    ]
