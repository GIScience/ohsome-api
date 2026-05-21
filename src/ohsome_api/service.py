from datetime import datetime

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api import db
from ohsome_api.db import generate_timestamp_series
from ohsome_api.models import RowModel


async def get_latest_timestamp() -> datetime:
    return await db.get_latest_timestamp()


async def get_currentness(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    bin_size: str | None,
    aoi_wkt: str,
) -> list[RowModel]:
    query_where_clause, query_args = ohsome_filter_to_sql(ohsome_filter)
    series = await generate_timestamp_series(start, end, bin_size)
    return await db.get_currentness(
        query_where_clause,
        query_args,
        start,
        end,
        series,
        aoi_wkt,
    )
