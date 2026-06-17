from datetime import datetime

from fastapi.concurrency import run_in_threadpool
from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api import db
from ohsome_api.db import generate_timestamp_series
from ohsome_api.models import ExtractionWriter, FeaturesRowModel, TimeBinsRowModel
from ohsome_api.request_models import Measure


async def get_latest_timestamp() -> datetime:
    return await db.get_latest_timestamp()


async def get_currentness(  # noqa: PLR0913
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    bin_size: str | None,
    aoi_wkt: str,
    measure: Measure,
) -> list[TimeBinsRowModel]:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    series = await generate_timestamp_series(start, end, bin_size)
    return await db.get_currentness(
        query_where_clause,
        query_args,
        start,
        end,
        series,
        aoi_wkt,
        measure,
    )


async def get_users_activity(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    bin_size: str | None,
    aoi_wkt: str,
) -> list[TimeBinsRowModel]:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    series = await generate_timestamp_series(start, end, bin_size)
    return await db.get_users_activity(
        query_where_clause,
        query_args,
        start,
        end,
        series,
        aoi_wkt,
    )


async def get_features(  # noqa: PLR0913
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    interval: str | None,
    aoi_wkt: str,
    measure: Measure,
) -> list[FeaturesRowModel]:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    series = await generate_timestamp_series(start, end, interval)
    return await db.get_features(
        query_where_clause,
        query_args,
        start,
        end,
        series,
        aoi_wkt,
        measure,
    )


async def get_extracted_features(
    ohsome_filter: OhsomeFilter,
    writer: ExtractionWriter,
) -> None:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    try:
        async for batch in db.get_extracted_features(query_where_clause, query_args):
            # https://starlette.dev/threadpool/
            # PERF: potentially we want to have our own thread pool
            await run_in_threadpool(writer.write_batch, batch)
    finally:
        await run_in_threadpool(writer.close)
