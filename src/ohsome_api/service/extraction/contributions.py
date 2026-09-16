from datetime import datetime
from typing import AsyncIterator, Literal

from ohsome_filter_to_sql import OhsomeFilter

from ohsome_api.db.extraction.contributions import (
    extract_contributions as extract_contributions_,
)
from ohsome_api.db.extraction.contributions import (
    join_changesets_to_extraction_rows,
)
from ohsome_api.models import ExtractionRow
from ohsome_api.parquet import ContributionParquetSink, Sink


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
