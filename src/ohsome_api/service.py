from datetime import datetime

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api import db
from ohsome_api.db import generate_timestamp_series
from ohsome_api.models import RowModel


async def get_latest_timestamp() -> datetime:
    return await db.get_latest_timestamp()


async def get_contributions_count(
    ohsome_filter: OhsomeFilter,
    start_timestamp: datetime,
    end_timestamp: datetime,
    bucket_size: str | None,
) -> list[RowModel]:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    series = await generate_timestamp_series(
        start_timestamp, end_timestamp, bucket_size
    )
    return await db.get_contributions_count(
        query_where_clause,
        query_args,
        start_timestamp,
        end_timestamp,
        series,
    )
