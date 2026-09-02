from datetime import datetime
from typing import AsyncIterator, Literal, Optional, cast

from ohsome_filter_to_sql import OhsomeFilter

from ohsome_api.db.extraction.contributions import (
    extract_contributions as extract_contributions_,
)
from ohsome_api.db.extraction.contributions import (
    join_changesets_to_extraction_rows,
)
from ohsome_api.db.extraction.features import (
    extract_features as extract_features_,
)
from ohsome_api.db.extraction.features_collection import (
    extract_features_collection as extract_features_collection_,
)
from ohsome_api.db.extraction.features_collection import (
    extract_features_collection_members_collections,
    extract_features_collection_members_features,
)
from ohsome_api.db.metadata.metadata import get_metadata
from ohsome_api.db.stats.contributions import get_contributions_count
from ohsome_api.db.stats.contributors import get_contributors_count
from ohsome_api.db.stats.currentness import get_currentness
from ohsome_api.db.stats.features import get_features, get_features_grouped_by_tag
from ohsome_api.db.time import generate_timestamp_series, get_latest_timestamp
from ohsome_api.models import (
    ExtractionRow,
    MeasureEnum,
    Metadata,
    SnapshotColumns,
    SnapshotColumnsGrouped,
    SnapshotRow,
    SnapshotRowGroupedByTag,
    TimeBinColumns,
    TimeBinRow,
)
from ohsome_api.parquet import (
    ArrowSink,
    ContributionParquetSink,
    MemberArrowSink,
    MemberParquetSink,
    ParquetSink,
    Sink,
)
from ohsome_api.request_models import GroupByTagModel


async def get_ohsomedb_metadata() -> Metadata:
    metadata = await get_metadata()
    return Metadata(**metadata)


async def get_currentness_row(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
    measure: MeasureEnum,
    clip: bool,
) -> list[TimeBinRow]:
    columns = await get_currentness_columns(
        ohsome_filter, start, end, bin_size, aoi_wkt, measure, clip
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
    clip: bool,
) -> TimeBinColumns:
    if end == "latest":
        end = await get_latest_timestamp()
    series = await generate_timestamp_series(start, end, bin_size)

    return await get_currentness(
        ohsome_filter,
        start,
        end,
        series,
        aoi_wkt,
        measure,
        clip,
    )


async def get_contributors_count_rows(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
) -> list[TimeBinRow]:
    columns = await get_contributors_count_columns(
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


async def get_contributors_count_columns(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
) -> TimeBinColumns:
    if end == "latest":
        end = await get_latest_timestamp()
    series = await generate_timestamp_series(start, end, bin_size)

    return await get_contributors_count(
        ohsome_filter,
        start,
        end,
        series,
        aoi_wkt,
    )


async def get_features_rows(
    ohsome_filter: OhsomeFilter,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    interval: str | None,
    aoi_wkt: str,
    measure: MeasureEnum,
    group_by: Optional[GroupByTagModel],
    clip: bool,
) -> list[SnapshotRow] | list[SnapshotRowGroupedByTag]:
    columns = await get_features_columns(
        ohsome_filter, start, end, interval, aoi_wkt, measure, group_by, clip
    )

    if group_by is not None:
        columns_grouped: SnapshotColumnsGrouped = cast(SnapshotColumnsGrouped, columns)
        timestamps = columns.timestamp
        result: list[SnapshotRowGroupedByTag] = []
        if columns_grouped.values is not None:
            for tagvalue in columns_grouped.values:
                result = result + [
                    SnapshotRowGroupedByTag(timestamp=ts, value=val, tagvalue=tagvalue)
                    for (ts, val) in zip(
                        timestamps, columns_grouped.values[tagvalue], strict=True
                    )
                ]
        result = result + [
            SnapshotRowGroupedByTag(timestamp=ts, value=val, tagvalue="")
            for (ts, val) in zip(timestamps, columns.value, strict=True)
        ]
        return result
    else:
        return [
            SnapshotRow(timestamp=ts, value=val)
            for (ts, val) in zip(columns.timestamp, columns.value, strict=True)
        ]


async def get_features_columns(
    ohsome_filter: OhsomeFilter,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    interval: str | None,
    aoi_wkt: str,
    measure: MeasureEnum,
    group_by: Optional[GroupByTagModel],
    clip: bool,
) -> SnapshotColumns:

    if start == "latest":
        start = await get_latest_timestamp()
        end = start
    elif end == "latest":
        end = await get_latest_timestamp()

    series = await generate_timestamp_series(start, end, interval)

    if group_by is None:
        return await get_features(
            ohsome_filter,
            series,
            aoi_wkt,
            measure,
            clip,
        )
    else:
        return await get_features_grouped_by_tag(
            ohsome_filter,
            series,
            aoi_wkt,
            measure,
            group_by.key,
            clip,
        )


async def extract_features(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""

    producer = extract_features_(
        ohsome_filter, aoi_wkt, clip, start, end, contributions
    )

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(producer)

    async def stream(first: list[ExtractionRow]) -> AsyncIterator[bytes]:
        with sink_type() as sink:
            yield sink.write_batch(first)

            async for batch in producer:
                yield sink.write_batch(batch)

        # after sink is closed metadata and footer is written
        yield sink.read_bytes()

    return stream(first_batch)


async def extract_features_as_parquet(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
) -> AsyncIterator[bytes]:
    return await extract_features(
        ohsome_filter, aoi_wkt, clip, start, end, contributions, ParquetSink
    )


async def extract_features_as_arrow(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
) -> AsyncIterator[bytes]:
    return await extract_features(
        ohsome_filter, aoi_wkt, clip, start, end, contributions, ArrowSink
    )


async def extract_features_collection(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""

    collections_producer = extract_features_collection_(ohsome_filter, aoi_wkt, time)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(collections_producer)

    async def stream(first: list[ExtractionRow]) -> AsyncIterator[bytes]:

        with sink_type() as sink:
            yield sink.write_batch(
                await extract_features_collection_members_collections(
                    first,
                    member_filter,
                    aoi_wkt,
                    clip,
                    time,
                )
            )

            async for batch in collections_producer:
                yield sink.write_batch(
                    await extract_features_collection_members_collections(
                        batch,
                        member_filter,
                        aoi_wkt,
                        clip,
                        time,
                    )
                )

        # after sink is closed metadata and footer is written
        yield sink.read_bytes()

    return stream(first_batch)


async def extract_features_collections_as_parquet(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collection(
        ohsome_filter, member_filter, aoi_wkt, clip, time, ParquetSink
    )


async def extract_features_collections_as_arrow(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collection(
        ohsome_filter, member_filter, aoi_wkt, clip, time, ArrowSink
    )


async def extract_features_collections_members(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""

    collections_producer = extract_features_collection_(ohsome_filter, aoi_wkt, time)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(collections_producer)

    async def stream(first: list[ExtractionRow]) -> AsyncIterator[bytes]:

        with sink_type() as sink:
            async for members in extract_features_collection_members_features(
                first,
                member_filter,
                aoi_wkt,
                clip,
                time,
            ):
                yield sink.write_batch(members)

            async for batch in collections_producer:
                async for member in extract_features_collection_members_features(
                    batch,
                    member_filter,
                    aoi_wkt,
                    clip,
                    time,
                ):
                    yield sink.write_batch(member)

        # after sink is closed metadata and footer is written
        yield sink.read_bytes()

    return stream(first_batch)


async def extract_features_collections_members_as_parquet(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collections_members(
        ohsome_filter, member_filter, aoi_wkt, clip, time, MemberParquetSink
    )


async def extract_features_collections_members_as_arrow(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collections_members(
        ohsome_filter, member_filter, aoi_wkt, clip, time, MemberArrowSink
    )


async def extract_contributions(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    start: datetime,
    end: datetime | Literal["latest"],
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:

    producer = extract_contributions_(ohsome_filter, aoi_wkt, start, end)

    first_batch = await anext(producer)

    async def stream(first: list[ExtractionRow]) -> AsyncIterator[bytes]:
        with sink_type() as sink:
            yield sink.write_batch(await join_changesets_to_extraction_rows(first))

            async for batch in producer:
                yield sink.write_batch(await join_changesets_to_extraction_rows(batch))

        # after sink is closed metadata and footer is written
        yield sink.read_bytes()

    return stream(first_batch)


async def extract_contributions_as_parquet(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    start: datetime,
    end: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_contributions(
        ohsome_filter,
        aoi_wkt,
        start,
        end,
        ContributionParquetSink,
    )


async def get_contributions_count_columns(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
) -> TimeBinColumns:

    if end == "latest":
        end = await get_latest_timestamp()
    series = await generate_timestamp_series(start, end, bin_size)

    return await get_contributions_count(
        ohsome_filter,
        start,
        end,
        series,
        aoi_wkt,
    )


async def get_contributions_count_rows(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
) -> list[TimeBinRow]:
    columns = await get_contributions_count_columns(
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
