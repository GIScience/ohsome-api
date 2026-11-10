from datetime import datetime
from pathlib import Path

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql
from shapely.geometry import MultiPolygon, Polygon

from ohsome_api.config import CONFIG
from ohsome_api.db.clauses import (
    disable_time_range_check_flag,
    get_aggregation_clause,
    get_geometry_clause,
)
from ohsome_api.db.db import db
from ohsome_api.db.errors import ResultTooLargeError
from ohsome_api.models import (
    Measure,
    TimeSeriesGroupedByResult,
    TimeSeriesResult,
)

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "features.sql").read_text()
SQL_QUERY_TEMPLATE_GROUP_BY = Path(
    Path(__file__).parent / "features_group_by.sql"
).read_text()


async def get_features(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi: Polygon | MultiPolygon,
    measure: Measure,
    clip: bool,
) -> TimeSeriesResult:
    geometry_clause, geometry_args = get_geometry_clause(aoi, shift=4)
    filter_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter, args_shift=(4 + len(geometry_args))
    )
    aggregation_clause = get_aggregation_clause(measure, clip)
    sql = SQL_QUERY_TEMPLATE % {
        "aggregation_clause": aggregation_clause,
        "geometry_clause": geometry_clause,
        "filter_clause": filter_clause,
        "aoi_join": "CROSS JOIN aoi" if clip else "",
        "disable_time_boundary_check": disable_time_range_check_flag(start, end),
    }

    records = await db.fetch_rows(
        sql,
        aoi,
        start,
        end,
        series,
        *geometry_args,
        *filter_args,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_series = {ts: 0 for ts in series}

    for record in records:
        zerofilled_series[record["ts"]] = record["value"]

    timestamps: list[datetime] = list(zerofilled_series.keys())
    values: list[int] = list(zerofilled_series.values())
    return TimeSeriesResult(timestamp=timestamps, value=values)


async def get_features_grouped_by_tag(  # noqa: PLR0915
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi: Polygon | MultiPolygon,
    measure: Measure,
    group_by_tag: str,
    clip: bool,
) -> TimeSeriesGroupedByResult:
    geometry_clause, geometry_args = get_geometry_clause(aoi, shift=5)
    filter_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter, args_shift=(5 + len(geometry_args))
    )
    aggregation_clause = get_aggregation_clause(measure, clip)
    limit = CONFIG.group_by_time_series_size_limit + 1
    sql = SQL_QUERY_TEMPLATE_GROUP_BY % {
        "aggregation_clause": aggregation_clause,
        "geometry_clause": geometry_clause,
        "filter_clause": filter_clause,
        "aoi_join": "CROSS JOIN aoi" if clip else "",
        "disable_time_boundary_check": disable_time_range_check_flag(start, end),
        "limit": limit,
    }
    records = await db.fetch_rows(
        sql,
        aoi,
        start,
        end,
        series,
        group_by_tag,
        *geometry_args,
        *filter_args,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_totals = {ts: 0 for ts in series}
    groups: set[str] = set()

    for record in records:
        zerofilled_totals[record["ts"]] = (
            zerofilled_totals[record["ts"]] + record["value"]
        )
        groups.add(record["grp"])

    if len(groups) * len(series) > limit:
        raise ResultTooLargeError(
            "The provided query produced too many results. The given "
            "time series parameters in combination with the "
            f"group by tags parameter lead to a result larger than {limit} rows."
        )

    zerofilled_results: dict[str, dict[datetime, int]] = dict()
    for group in groups:
        zerofilled_results[group] = {ts: 0 for ts in series}
    for record in records:
        zerofilled_results[record["grp"]][record["ts"]] = record["value"]

    timestamps: list[datetime] = list(zerofilled_totals.keys())
    total_values: list[int] = list(zerofilled_totals.values())
    group_by_values: dict[str, list[int]] = {
        group: list(x.values())
        for (group, x) in zerofilled_results.items()
        if group is not None
    }
    return TimeSeriesGroupedByResult(
        timestamp=timestamps,
        value=total_values,
        group=dict(sorted(group_by_values.items())),
    )
