from datetime import datetime
from pathlib import Path

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.db.db import db
from ohsome_api.db.stats.utils import (
    get_aggregation_clause,
    zerofill_records_to_time_bin_columns,
)
from ohsome_api.models import (
    MeasureEnum,
    TimeBinColumns,
)

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "currentness.sql").read_text()


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
