from datetime import datetime
from pathlib import Path

from asyncpg import Record
from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.database import db
from ohsome_api.models import (
    MeasureEnum,
    TimeBinColumns,
)
from ohsome_api.ohsomedb.stats.utils import get_aggregation_clause

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "currentness.sql").read_text()


def zerofill_records_to_time_bin_columns(
    records: list[Record],
    series: list[datetime],
) -> TimeBinColumns:
    zerofilled_series = {i: 0 for i in range(len(series) - 1)}

    for record in records:
        zerofilled_series[record["time_bin"] - 1] = record["value"]

    start_timestamps: list[datetime] = [
        series[time_bin] for time_bin in zerofilled_series
    ]

    end_timestamps: list[datetime] = [
        series[time_bin + 1] for time_bin in zerofilled_series
    ]

    values: list[int] = list(zerofilled_series.values())

    return TimeBinColumns(start=start_timestamps, end=end_timestamps, value=values)


async def get_currentness(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
    clip: bool,
) -> TimeBinColumns:
    filter_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=4)
    aggregation_clause = get_aggregation_clause(measure, clip)
    sql = SQL_QUERY_TEMPLATE % {
        "aggregation_clause": aggregation_clause,
        "filter_clause": filter_clause,
    }
    records = await db.fetch_rows(
        sql,
        start,
        end,
        series,
        aoi_wkt,
        *filter_args,
    )  # order matters!

    return zerofill_records_to_time_bin_columns(records, series)
