from datetime import datetime
from typing import Literal

from ohsome_filter_to_sql import OhsomeFilter

from ohsome_api.db.stats.contributors import get_contributors_count
from ohsome_api.db.time import generate_timestamp_series, get_latest_timestamp
from ohsome_api.models import TimeBinsResult, TimeBinsRowResult


async def get_contributors_count_rows(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime | Literal["latest"],
    bin_size: str | None,
    aoi_wkt: str,
) -> list[TimeBinsRowResult]:
    columns = await get_contributors_count_columns(
        ohsome_filter,
        start,
        end,
        bin_size,
        aoi_wkt,
    )
    return [
        TimeBinsRowResult(start=start, end=end, value=val)
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
) -> TimeBinsResult:
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
