from datetime import datetime
from typing import AsyncIterator, Literal

from ohsome_filter_to_sql import OhsomeFilter

from ohsome_api.db.extraction.features import extract_features as extract_features_
from ohsome_api.models import ExtractionRow
from ohsome_api.parquet import ArrowSink, ParquetSink, Sink


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
