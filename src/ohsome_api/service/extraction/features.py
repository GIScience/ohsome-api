from datetime import datetime
from typing import AsyncIterator, Literal

from ohsome_filter_to_sql import OhsomeFilter
from shapely.geometry import MultiPolygon, Polygon

from ohsome_api.db.extraction.features import extract_features as extract_features_
from ohsome_api.parquet import FEATURES, ArrowSink, ParquetSink, Sink
from ohsome_api.service.extraction.streaming import encode, prepend


async def extract_features(
    ohsome_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:
    """Extract features from database batch wise."""

    producer = extract_features_(ohsome_filter, aoi, clip, start, end, contributions)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(producer)

    return encode(sink_type, FEATURES, prepend(first_batch, producer))


async def extract_features_as_parquet(
    ohsome_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
) -> AsyncIterator[bytes]:
    return await extract_features(
        ohsome_filter, aoi, clip, start, end, contributions, ParquetSink
    )


async def extract_features_as_arrow(
    ohsome_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
) -> AsyncIterator[bytes]:
    return await extract_features(
        ohsome_filter, aoi, clip, start, end, contributions, ArrowSink
    )
