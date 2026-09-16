from datetime import datetime
from typing import AsyncIterator, Literal

from ohsome_filter_to_sql import OhsomeFilter

from ohsome_api.db.extraction.features_collection import (
    extract_features_collection as extract_features_collection_,
)
from ohsome_api.db.extraction.features_collection import (
    extract_features_collection_members_collections,
    extract_features_collection_members_features,
)
from ohsome_api.models import ExtractionRow
from ohsome_api.parquet import (
    ArrowSink,
    MemberArrowSink,
    MemberParquetSink,
    ParquetSink,
    Sink,
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
