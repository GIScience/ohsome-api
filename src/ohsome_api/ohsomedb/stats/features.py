from datetime import datetime
from pathlib import Path

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.database import db
from ohsome_api.models import (
    MeasureEnum,
    SnapshotColumns,
)
from ohsome_api.ohsomedb.stats.utils import get_aggregation_clause

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "features.sql").read_text()


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
