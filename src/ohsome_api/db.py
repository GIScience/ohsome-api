from datetime import datetime

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.models import RowModel

SCHEMA = CONFIG.ohsomedb.schemaname


async def get_latest_timestamp() -> datetime:
    sql = f"SELECT last_timestamp FROM {SCHEMA}.contributions_state"  # noqa: S608
    record = await db.fetch_row(sql)
    if not isinstance(record["last_timestamp"], datetime):
        raise TypeError()
    return record["last_timestamp"]


async def get_contributions_count(
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    period: str,
) -> list[RowModel]:
    filter_args_count = len(filter_args)
    # TODO: do the timeseries generation in python, then we can drop
    # get_contributions_count_single_interval
    sql = f"""
        WITH series AS (
            SELECT
            array_agg(ts) AS times
            FROM generate_series(
                ${filter_args_count + 1}::timestamptz,
                ${filter_args_count + 2}::timestamptz,
                (${filter_args_count + 3}::text)::interval
            ) ts
        )
        SELECT
            i.count AS count,
            series.times[i.time_bin] AS time_bin_start,
            series.times[i.time_bin + 1] AS time_bin_end
        FROM (
            SELECT
                COUNT(*) AS count,
                width_bucket(valid_from, series.times) AS time_bin
            FROM {SCHEMA}.contributions
            CROSS JOIN series
            WHERE ({filter_where_clause})
            AND valid_from BETWEEN ${filter_args_count + 1}::timestamptz
                               AND ${filter_args_count + 2}::timestamptz
            GROUP BY time_bin
            ORDER BY time_bin
        ) i
        CROSS JOIN series
    """  # noqa: S608
    records = await db.fetch_rows(
        sql,
        *filter_args,
        start,
        end,
        period,
    )  # order matters!

    return [
        RowModel(
            value=record["count"],
            start=record["time_bin_start"],
            end=record["time_bin_end"],
        )
        for record in records
    ]


async def get_contributions_count_single_interval(
    filter_where_clause: str, filter_args: tuple, start: datetime, end: datetime
) -> list[RowModel]:
    filter_args_count = len(filter_args)
    sql = f"""
        SELECT
            COUNT(*) AS count
        FROM {SCHEMA}.contributions
        WHERE ({filter_where_clause})
        AND valid_from BETWEEN ${filter_args_count + 1}::timestamptz
                           AND ${filter_args_count + 2}::timestamptz
    """  # noqa: S608
    record = await db.fetch_row(
        sql,
        *filter_args,
        start,
        end,
    )  # order matters!

    return [
        RowModel(
            value=record["count"],
            start=start,
            end=end,
        )
    ]
