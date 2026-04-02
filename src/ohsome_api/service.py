from datetime import datetime

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api import db


def get_latest_timestamp() -> datetime:
    return db.get_latest_timestamp()


def get_contributions_count(ohsome_filter: OhsomeFilter) -> int:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    return db.get_contributions_count(query_where_clause, query_args)
