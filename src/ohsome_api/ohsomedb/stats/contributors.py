from datetime import datetime
from pathlib import Path

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.database import db
from ohsome_api.models import TimeBinColumns
from ohsome_api.ohsomedb.stats.utils import zerofill_records_to_time_bin_columns

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "contributors.sql").read_text()


async def get_contributors_count(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
) -> TimeBinColumns:
    filter_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=4)
    filter_clause_tags_before = filter_clause.replace("tags", "tags_before")
    sql = SQL_QUERY_TEMPLATE % {
        "filter_clause": filter_clause,
        "filter_clause_tags_before": filter_clause_tags_before,
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
