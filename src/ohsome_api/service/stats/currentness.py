from datetime import datetime
from typing import Literal

from ohsome_filter_to_sql import OhsomeFilter

from ohsome_api.db.stats.currentness import get_currentness
from ohsome_api.db.time import generate_timestamp_series, get_latest_timestamp
from ohsome_api.models import Measure, TimeBinsResult, TimeBinsRowResult


async def get_currentness_row(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
    measure: Measure,
    clip: bool,
) -> list[TimeBinsRowResult]:
    columns = await get_currentness_columns(
        ohsome_filter, start, end, bin_size, aoi_wkt, measure, clip
    )
    return [
        TimeBinsRowResult(start=start, end=end, value=val)
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
    measure: Measure,
    clip: bool,
) -> TimeBinsResult:
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
