from datetime import datetime
from typing import Literal, cast

from ohsome_filter_to_sql import OhsomeFilter

from ohsome_api.db.stats.features import get_features, get_features_grouped_by_tag
from ohsome_api.db.time import generate_timestamp_series, get_latest_timestamp
from ohsome_api.models import (
    Measure,
    TimeSeriesGroupedByResult,
    TimeSeriesResult,
    TimeSeriesRowGroupedByResult,
    TimeSeriesRowResult,
)
from ohsome_api.request_models import GroupByTag


async def get_features_rows(
    ohsome_filter: OhsomeFilter,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    interval: str | None,
    aoi_wkt: str,
    measure: Measure,
    group_by: GroupByTag | None,
    clip: bool,
) -> list[TimeSeriesRowResult] | list[TimeSeriesRowGroupedByResult]:
    columns = await get_features_columns(
        ohsome_filter, start, end, interval, aoi_wkt, measure, group_by, clip
    )

    if group_by is None:
        return [
            TimeSeriesRowResult(timestamp=ts, value=val)
            for (ts, val) in zip(columns.timestamp, columns.value, strict=True)
        ]

    columns_grouped: TimeSeriesGroupedByResult = cast(
        TimeSeriesGroupedByResult, columns
    )
    timestamps = columns.timestamp
    result: list[TimeSeriesRowGroupedByResult] = []
    if columns_grouped.group is not None:
        for group in columns_grouped.group:
            result = result + [
                TimeSeriesRowGroupedByResult(timestamp=ts, value=val, group=group)
                for (ts, val) in zip(
                    timestamps, columns_grouped.group[group], strict=True
                )
            ]
    result = result + [
        TimeSeriesRowGroupedByResult(timestamp=ts, value=val, group="")
        for (ts, val) in zip(timestamps, columns.value, strict=True)
    ]

    return result


async def get_features_columns(
    ohsome_filter: OhsomeFilter,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    interval: str | None,
    aoi_wkt: str,
    measure: Measure,
    group_by: GroupByTag | None,
    clip: bool,
) -> TimeSeriesResult:

    if start == "latest":
        start = await get_latest_timestamp()
        end = start
    elif end == "latest":
        end = await get_latest_timestamp()

    series = await generate_timestamp_series(start, end, interval)

    if group_by is None:
        return await get_features(
            ohsome_filter,
            start,
            end,
            series,
            aoi_wkt,
            measure,
            clip,
        )

    return await get_features_grouped_by_tag(
        ohsome_filter,
        start,
        end,
        series,
        aoi_wkt,
        measure,
        group_by.key,
        clip,
    )
