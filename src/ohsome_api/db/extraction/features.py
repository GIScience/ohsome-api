from datetime import datetime
from pathlib import Path
from typing import AsyncIterator, Literal, cast

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.db.clauses import _filter_by_time
from ohsome_api.db.db import db
from ohsome_api.models import ExtractionRow

SQL_QUERY_TEMPLATE_CLIP = Path(Path(__file__).parent / "features_clip.sql").read_text()
SQL_QUERY_TEMPLATE_NO_CLIP = Path(
    Path(__file__).parent / "features_no_clip.sql"
).read_text()


def extract_features(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
) -> AsyncIterator[list[ExtractionRow]]:
    """Extract all features"""

    time_clause, time_args = _filter_by_time(start, end, contributions)
    filter_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter,
        args_shift=len(time_args) + 1,
    )

    if clip:
        sql = SQL_QUERY_TEMPLATE_CLIP % {
            "filter_clause": filter_clause,
            "time_clause": time_clause,
        }
    else:
        sql = SQL_QUERY_TEMPLATE_NO_CLIP % {
            "filter_clause": filter_clause,
            "time_clause": time_clause,
        }

    # cast generic asyncpg Record to ExtractionRow
    # TODO: make batch size configurable (maybe as function arg)
    return cast(
        AsyncIterator[list[ExtractionRow]],
        # PERF: batch_size should be different depending on expected row size
        #   (e.g. GeometryType)
        db.fetch_batch(sql, aoi_wkt, *time_args, *filter_args, batch_size=10000),
    )
