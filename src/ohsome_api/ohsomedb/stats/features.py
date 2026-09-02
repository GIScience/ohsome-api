from datetime import datetime
from pathlib import Path

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.models import (
    MeasureEnum,
    SnapshotColumns,
    SnapshotColumnsGrouped,
)
from ohsome_api.ohsomedb.errors import ResultTooLargeError
from ohsome_api.ohsomedb.stats.utils import get_aggregation_clause

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "features.sql").read_text()
SQL_QUERY_TEMPLATE_GROUP_BY = Path(
    Path(__file__).parent / "features_group_by.sql"
).read_text()


async def get_features(
    ohsome_filter: OhsomeFilter,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
    clip: bool,
) -> SnapshotColumns:
    filter_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=2)
    aggregation_clause = get_aggregation_clause(measure, clip)
    sql = SQL_QUERY_TEMPLATE % {
        "filter_clause": filter_clause,
        "aggregation_clause": aggregation_clause,
    }
    records = await db.fetch_rows(
        sql,
        series,
        aoi_wkt,
        *filter_args,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_series = {ts: 0 for ts in series}

    for record in records:
        zerofilled_series[record["ts"]] = record["value"]

    timestamps: list[datetime] = list(zerofilled_series.keys())
    values: list[int] = list(zerofilled_series.values())
    return SnapshotColumns(timestamp=timestamps, value=values)


async def get_features_grouped_by_tag(
    ohsome_filter: OhsomeFilter,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
    group_by_tag: str,
    clip: bool,
) -> SnapshotColumnsGrouped:
    filter_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=3)
    aggregation_clause = get_aggregation_clause(measure, clip)
    limit = CONFIG.group_by_time_series_size_limit + 1
    sql = SQL_QUERY_TEMPLATE_GROUP_BY % {
        "filter_clause": filter_clause,
        "aggregation_clause": aggregation_clause,
        "limit": limit,
    }
    records = await db.fetch_rows(
        sql,
        series,
        aoi_wkt,
        group_by_tag,
        *filter_args,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_totals = {ts: 0 for ts in series}
    all_tags: set[str] = set()

    for record in records:
        zerofilled_totals[record["ts"]] = (
            zerofilled_totals[record["ts"]] + record["value"]
        )
        all_tags.add(record["tag_value"])

    if len(all_tags) * len(series) > limit:
        raise ResultTooLargeError(
            "The provided query produced too many results. The given "
            "time series parameters in combination with the "
            f"group by tags parameter lead to a result larger than {limit} rows."
        )

    zerofilled_results: dict[str, dict[datetime, int]] = dict()
    for tag_value in all_tags:
        zerofilled_results[tag_value] = {ts: 0 for ts in series}
    for record in records:
        zerofilled_results[record["tag_value"]][record["ts"]] = record["value"]

    timestamps: list[datetime] = list(zerofilled_totals.keys())
    total_values: list[int] = list(zerofilled_totals.values())
    group_by_values: dict[str, list[int]] = {
        value: list(x.values())
        for (value, x) in zerofilled_results.items()
        if value is not None
    }
    return SnapshotColumnsGrouped(
        timestamp=timestamps,
        value=total_values,
        values=group_by_values,
    )
