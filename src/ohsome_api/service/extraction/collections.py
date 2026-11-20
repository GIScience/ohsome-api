from datetime import datetime
from typing import AsyncIterator, Literal

from ohsome_filter_to_sql import OhsomeFilter
from shapely.geometry import MultiPolygon, Polygon

from ohsome_api.db.extraction.features_collection import (
    extract_features_collection as extract_features_collection_,
)
from ohsome_api.db.extraction.features_collection import (
    extract_features_collection_members_collections,
    extract_features_collection_members_features,
)
from ohsome_api.models import ExtractionRow
from ohsome_api.parquet import FEATURES, MEMBERS, ArrowSink, ParquetSink, Sink
from ohsome_api.service.extraction.streaming import (
    Batch,
    encode,
    map_batches,
    prepend,
)


async def extract_features_collection(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    time: datetime | Literal["latest"],
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""

    collections_producer = extract_features_collection_(ohsome_filter, aoi, time)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(collections_producer)

    async def with_members(collections: Batch) -> list[ExtractionRow]:
        return await extract_features_collection_members_collections(
            collections,
            member_filter,
            aoi,
            clip,
            time,
        )

    batches = map_batches(prepend(first_batch, collections_producer), with_members)
    return encode(sink_type, FEATURES, batches)


async def extract_features_collections_as_parquet(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collection(
        ohsome_filter, member_filter, aoi, clip, time, ParquetSink
    )


async def extract_features_collections_as_arrow(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collection(
        ohsome_filter, member_filter, aoi, clip, time, ArrowSink
    )


async def extract_features_collections_members(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    time: datetime | Literal["latest"],
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""

    collections_producer = extract_features_collection_(ohsome_filter, aoi, time)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(collections_producer)

    async def members(batches: AsyncIterator[Batch]) -> AsyncIterator[Batch]:
        # one batch of collections results in several batches of members
        async for collections in batches:
            async for batch in extract_features_collection_members_features(
                collections,
                member_filter,
                aoi,
                clip,
                time,
            ):
                yield batch
                del batch
            del collections

    batches = members(prepend(first_batch, collections_producer))
    return encode(sink_type, MEMBERS, batches)


async def extract_features_collections_members_as_parquet(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collections_members(
        ohsome_filter, member_filter, aoi, clip, time, ParquetSink
    )


async def extract_features_collections_members_as_arrow(
    ohsome_filter: OhsomeFilter,
    member_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_features_collections_members(
        ohsome_filter, member_filter, aoi, clip, time, ArrowSink
    )
