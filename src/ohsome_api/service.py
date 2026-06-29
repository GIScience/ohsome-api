from datetime import datetime
from typing import AsyncIterator

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api import db
from ohsome_api.db import generate_timestamp_series
from ohsome_api.models import (
    ExtractionRow,
    Metadata,
    SnapshotRow,
    TimeBinRow,
)
from ohsome_api.parquet import ArrowSink, ParquetSink
from ohsome_api.request_models import Measure

# TODO: Should we pass AOI as some kind of Geom Type instead of str through to DB?


async def get_ohsomedb_metadata() -> Metadata:
    metadata = await db.get_metadata()
    return Metadata(**metadata)


async def get_currentness(  # noqa: PLR0913
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    bin_size: str | None,
    aoi_wkt: str,
    measure: Measure,
) -> list[TimeBinRow]:
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
) -> list[TimeBinRow]:
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
) -> list[SnapshotRow]:
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


async def extract_features_as_parquet(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    producer = db.extract_features(query_where_clause, query_args, aoi_wkt)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(producer)

    async def stream(first: list[ExtractionRow]) -> AsyncIterator[bytes]:
        with ParquetSink() as sink:
            yield sink.write_batch(first)

            async for batch in producer:
                yield sink.write_batch(batch)

        # after sink is closed metadata and footer is written
        yield sink.read_bytes()

    return stream(first_batch)


async def extract_features_as_arrow(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    producer = db.extract_features(query_where_clause, query_args, aoi_wkt)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(producer)

    async def stream(first: list[ExtractionRow]) -> AsyncIterator[bytes]:
        with ArrowSink() as sink:
            yield sink.write_batch(first)

            async for batch in producer:
                yield sink.write_batch(batch)

        # after sink is closed metadata and footer is written
        yield sink.read_bytes()

    return stream(first_batch)
