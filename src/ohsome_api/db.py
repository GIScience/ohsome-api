from datetime import datetime
from pathlib import Path
from typing import AsyncIterator, Literal, cast

from asyncpg import Record
from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.models import (
    ExtractionRow,
    MeasureEnum,
    TimeBinColumns,
)

QUERIES_DIR = Path(__file__).parent / "queries"


class TimeSeriesTooLargeError(ValueError):
    pass


class ResultTooLargeError(ValueError):
    pass


async def generate_timestamp_series(
    start: datetime,
    end: datetime,
    interval: str | None,
) -> list[datetime]:
    if start == end:
        return [start]

    if interval is None:
        return [start, end]

    limit = CONFIG.time_series_size_limit

    sql = """
        SELECT generate_series(
            $1::timestamptz,
            $2::timestamptz,
            ($3::text)::interval
        ) as ts
        LIMIT $4
    """
    records = await db.fetch_rows(sql, start, end, interval, limit + 1)

    if len(records) > limit:
        raise TimeSeriesTooLargeError(
            "The provided values for the time parameter (time bin or time series) "
            f"lead to a time series larger than {limit} points/bins."
        )

    # TODO: Extract post-processing to own function and write unit-tests
    results = [r["ts"] for r in records]
    if results[-1] != end:
        # include uneven time bin
        results.append(end)
    return results


async def get_latest_timestamp() -> datetime:
    sql = "SELECT last_timestamp FROM contributions_state"
    return (await db.fetch_row(sql))[0]


async def get_metadata() -> dict[str, datetime]:
    sql = Path(QUERIES_DIR / "get_metadata.sql").read_text()
    return dict(await db.fetch_row(sql))


def zerofill_records_to_time_bin_columns(
    records: list[Record], series: list[datetime]
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


def aggregation_clause(measure: MeasureEnum, clip: bool) -> str:
    match measure:
        case MeasureEnum.COUNT:
            return "COUNT(*) AS value"
        case MeasureEnum.LENGTH:
            # [m]
            if not clip:
                return """
                ROUND(SUM(c.length)) AS value
                """

            return """
            ROUND(
                SUM(
                    CASE
                        WHEN ST_Covers(
                            aoi.geom,
                            c.geom
                        )
                        THEN c.length -- Use precomputed length from ohsome-planet
                        ELSE ST_Length(
                            ST_Intersection(
                                c.geom,
                                aoi.geom
                            )::geography
                        )
                    END
                )
            ) AS value
            """
        case MeasureEnum.AREA:
            # [m²]
            if not clip:
                return """
                ROUND(SUM(c.area)) AS value
                """
            return """
            ROUND(
                SUM(
                    CASE
                        WHEN ST_Covers(
                            aoi.geom,
                            c.geom
                        )
                        THEN c.area -- Use precomputed area from ohsome-planet
                        ELSE ST_Area(
                            ST_Intersection(
                                c.geom,
                                aoi.geom
                            )::geography
                        )
                    END
                )
            ) AS value
            """


async def extract_contributions(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    start: datetime,
    end: datetime | Literal["latest"],
) -> AsyncIterator[list[ExtractionRow]]:

    if end == "latest":
        filter_by_time_constraint = """
            AND valid_from >= $2::timestamptz
        """
        time_args = [start]
    else:
        filter_by_time_constraint = """
            AND valid_from >= $2::timestamptz
            AND valid_from <  $3::timestamptz
        """
        time_args = [start, end]

    filter_where_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter,
        args_shift=len(time_args) + 1,
    )

    filter_by_time = f"""
        status_geom_type = ANY(array[
          ('latest','Point')::status_geom_type_type,
          ('latest','LineString')::status_geom_type_type,
          ('latest','Polygon')::status_geom_type_type,
          ('latest','MultiPolygon')::status_geom_type_type,
          ('history','Point')::status_geom_type_type,
          ('history','LineString')::status_geom_type_type,
          ('history','Polygon')::status_geom_type_type,
          ('history','MultiPolygon')::status_geom_type_type,
          ('deleted','Point')::status_geom_type_type,
          ('deleted','LineString')::status_geom_type_type,
          ('deleted','Polygon')::status_geom_type_type,
          ('deleted','MultiPolygon')::status_geom_type_type
       ])
       {filter_by_time_constraint}
    """

    sql = f"""
       WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($1, 4326))).geom as geom
       )
       SELECT osm_type,
              osm_id,
              valid_from,
              valid_to,
              osm_version,
              osm_minor_version,
              osm_edits,
              user_id,
              user_name,
              changeset_id,
              contrib_type,
              case when c.contrib_type = 'DELETION' then c.tags_before else c.tags end as tags,
              tags_before,
              (status_geom_type).geom_type as geom_type,
              ST_AsBinary(c.geom) as geom,
              ST_XMin(c.geom) as xmin,
              ST_YMin(c.geom) as ymin,
              ST_XMax(c.geom) as xmax,
              ST_YMax(c.geom) as ymax
       FROM contributions c
       JOIN aoi ON (ST_INTERSECTS(aoi.geom, c.geom))
       WHERE {filter_by_time}
         AND (
              ({filter_where_clause}) or
              -- HACK: ohsome-filter-to-sql does not know about tags before.
              -- Apply same tag filter to tags_before.
              -- In this case all other filter parts are duplicated
              -- and hopefully ignored by query planner.
              ({filter_where_clause.replace("tags", "tags_before")})
        )
    """  # noqa: E501, S608
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
