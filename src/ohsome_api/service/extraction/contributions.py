from datetime import datetime
from typing import AsyncIterator, Literal

from ohsome_filter_to_sql import OhsomeFilter
from shapely.geometry import MultiPolygon, Polygon

from ohsome_api.db.extraction.contributions import (
    extract_contributions as extract_contributions_,
)
from ohsome_api.db.extraction.contributions import (
    join_changesets_to_extraction_rows,
)
from ohsome_api.parquet import CONTRIBUTIONS, ParquetSink, Sink
from ohsome_api.service.extraction.streaming import encode, map_batches, prepend


async def extract_contributions(
    ohsome_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    start: datetime,
    end: datetime | Literal["latest"],
    sink_type: type[Sink],
) -> AsyncIterator[bytes]:

    producer = extract_contributions_(ohsome_filter, aoi, start, end)

    # try to fetch first batch to check if we could get connection from database pool
    first_batch = await anext(producer)

    batches = map_batches(
        prepend(first_batch, producer),
        join_changesets_to_extraction_rows,
    )
    return encode(sink_type, CONTRIBUTIONS, batches)


async def extract_contributions_as_parquet(
    ohsome_filter: OhsomeFilter,
    aoi: Polygon | MultiPolygon,
    start: datetime,
    end: datetime | Literal["latest"],
) -> AsyncIterator[bytes]:
    return await extract_contributions(
        ohsome_filter,
        aoi,
        start,
        end,
        ParquetSink,
    )
