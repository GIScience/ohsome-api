from datetime import datetime
from typing import AsyncIterator, Literal

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api import db
from ohsome_api.db import generate_timestamp_series, get_latest_timestamp
from ohsome_api.models import (
    ExtractionRow,
    MeasureEnum,
    Metadata,
    SnapshotColumns,
    SnapshotRow,
    TimeBinColumns,
    TimeBinRow,
)
from ohsome_api.parquet import ArrowSink, ParquetSink


async def get_ohsomedb_metadata() -> Metadata:
    metadata = await db.get_metadata()
    return Metadata(**metadata)


async def get_currentness_row(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
    measure: MeasureEnum,
) -> list[TimeBinRow]:
    columns = await get_currentness_columns(
        ohsome_filter, start, end, bin_size, aoi_wkt, measure
    )
    return [
        TimeBinRow(start=start, end=end, value=val)
        for start, end, val in zip(
            columns.start, columns.end, columns.value, strict=True
        )
    ]


async def get_currentness_columns(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
    measure: MeasureEnum,
) -> TimeBinColumns:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    if end == "latest":
        end = await get_latest_timestamp()
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


async def get_contributors_activity_rows(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
) -> list[TimeBinRow]:
    columns = await get_contributors_activity_columns(
        ohsome_filter,
        start,
        end,
        bin_size,
        aoi_wkt,
    )
    return [
        TimeBinRow(start=start, end=end, value=val)
        for start, end, val in zip(
            columns.start, columns.end, columns.value, strict=True
        )
    ]


async def get_contributors_activity_columns(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
) -> TimeBinColumns:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    if end == "latest":
        end = await get_latest_timestamp()
    series = await generate_timestamp_series(start, end, bin_size)

    return await db.get_contributors_activity(
        query_where_clause,
        query_args,
        start,
        end,
        series,
        aoi_wkt,
    )


async def get_features_rows(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    interval: str | None,
    aoi_wkt: str,
    measure: MeasureEnum,
) -> list[SnapshotRow]:
    columns = await get_features_columns(
        ohsome_filter, start, end, interval, aoi_wkt, measure
    )

    return [
        SnapshotRow(timestamp=ts, value=val)
        for ts, val in zip(columns.timestamp, columns.value, strict=True)
    ]


async def get_features_columns(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    interval: str | None,
    aoi_wkt: str,
    measure: MeasureEnum,
) -> SnapshotColumns:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    if end == "latest":
        end = await get_latest_timestamp()
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
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    producer = db.extract_features(query_where_clause, query_args, aoi_wkt, clip, time)

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
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    producer = db.extract_features(query_where_clause, query_args, aoi_wkt, clip, time)

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


async def extract_features_collections_as_parquet(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)

    collections_producer = db.extract_features_collection(
        query_where_clause, query_args, aoi_wkt, time
    )

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(collections_producer)

    async def stream(first: list[ExtractionRow]) -> AsyncIterator[bytes]:
        with ParquetSink() as sink:
            yield sink.write_batch(
                await db.extract_features_collection_members(
                    first,
                    aoi_wkt,
                    clip,
                    time,
                )
            )

            async for batch in collections_producer:
                yield sink.write_batch(
                    await db.extract_features_collection_members(
                        batch,
                        aoi_wkt,
                        clip,
                        time,
                    )
                )

        # after sink is closed metadata and footer is written
        yield sink.read_bytes()

    return stream(first_batch)
