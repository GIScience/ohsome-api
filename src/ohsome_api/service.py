from datetime import datetime

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api import db


async def get_latest_timestamp() -> datetime:
    return await db.get_latest_timestamp()


async def get_contributions_count(
    ohsome_filter: OhsomeFilter, start: datetime, end: datetime
) -> int:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    return await db.get_contributions_count(query_where_clause, query_args)
