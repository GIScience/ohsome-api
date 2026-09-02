# TODO: Factor out SQLs to files
from datetime import datetime
from pathlib import Path
from typing import AsyncIterator, Literal, cast

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.db.db import db
from ohsome_api.models import ExtractionRow

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "contributions.sql").read_text()


async def extract_contributions(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    start: datetime,
    end: datetime | Literal["latest"],
) -> AsyncIterator[list[ExtractionRow]]:

    if end == "latest":
        time_clause = """
            valid_from >= $2::timestamptz
        """
        time_args = [start]
    else:
        time_clause = """
            valid_from >= $2::timestamptz
            AND valid_from <  $3::timestamptz
        """
        time_args = [start, end]

    filter_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter,
        args_shift=len(time_args) + 1,
    )
    filter_clause_tags_before = filter_clause.replace("tags", "tags_before")

    sql = SQL_QUERY_TEMPLATE % {
        "time_clause": time_clause,
        "filter_clause": filter_clause,
        "filter_clause_tags_before": filter_clause_tags_before,
    }

    async for batch in db.fetch_batch(
        sql, aoi_wkt, *time_args, *filter_args, batch_size=10000
    ):
        yield [ExtractionRow(cast(ExtractionRow, item)) for item in batch]


async def join_changesets_to_extraction_rows(
    rows: list[ExtractionRow],
) -> list[ExtractionRow]:
    changeset_id: set[int] = {row["changeset_id"] for row in rows}
    records = await db.fetch_rows(
        """
        SELECT id as changeset_id, tags
        FROM changesets
        WHERE id = ANY($1::int[])
        """,
        changeset_id,
    )
    changeset_lookup = {row["changeset_id"]: row["tags"] for row in records}
    for row in rows:
        if tags := changeset_lookup.get(row["changeset_id"]):
            row["changeset_tags"] = tags
        else:
            row["changeset_tags"] = {}
    return rows
