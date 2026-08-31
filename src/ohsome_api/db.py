from datetime import datetime
from typing import AsyncIterator, Literal, cast

from asyncpg import Record
from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.models import (
    ExtractionRow,
    MeasureEnum,
    SnapshotColumns,
    SnapshotColumnsGrouped,
    TimeBinColumns,
)


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
    sql = """
        WITH
            first AS (
                SELECT value::timestamptz as first_timestamp
                FROM ohsomedb_metadata
                WHERE key = 'valid_from'
            ), last AS (
                SELECT last_timestamp
                FROM contributions_state
            )
        SELECT first_timestamp as start, last_timestamp as end FROM first, last;
"""
    return dict(await db.fetch_row(sql))


async def get_currentness(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
    clip: bool,
) -> TimeBinColumns:
    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=4)
    sql = f"""
        WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($4, 4326))).geom as geom
        )
        SELECT
            {aggregation_clause(measure, clip)},
            width_bucket(valid_from, $3::timestamptz[]) AS time_bin
        FROM contributions c, aoi
        WHERE ({filter_where_clause})
        AND valid_from >= $1::timestamptz and
            valid_from <  $2::timestamptz
        AND ST_Intersects(c.geom, aoi.geom)
        AND (status_geom_type).status = 'latest'
        GROUP BY time_bin
        ORDER BY time_bin
    """  # noqa: S608
    records = await db.fetch_rows(
        sql,
        start,
        end,
        series,
        aoi_wkt,
        *filter_args,
    )  # order matters!

    return zerofill_records_to_time_bin_columns(records, series)


async def get_contributors_count(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
) -> TimeBinColumns:
    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=4)
    sql = f"""
        WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($4, 4326))).geom as geom
        )
        SELECT
            count(distinct user_id) AS value,
            width_bucket(valid_from, $3::timestamptz[]) AS time_bin
        FROM contributions c
        JOIN aoi on (ST_Intersects(c.geom, aoi.geom))
        WHERE valid_from >= $1::timestamptz and
              valid_from <  $2::timestamptz
          AND (
              ({filter_where_clause}) or
              -- HACK: ohsome-filter-to-sql does not know about tags before.
              -- Apply same tag filter to tags_before.
              -- In this case all other filter parts are duplicated
              -- and hopefully ignored by query planner.
              ({filter_where_clause.replace("tags", "tags_before")})
          )
        GROUP BY time_bin
        ORDER BY time_bin
    """  # noqa: S608

    records = await db.fetch_rows(
        sql,
        start,
        end,
        series,
        aoi_wkt,
        *filter_args,
    )  # order matters!

    return zerofill_records_to_time_bin_columns(records, series)


async def get_contributions_count(
    ohsome_filter: OhsomeFilter,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
) -> TimeBinColumns:
    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=4)
    sql = f"""
        WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($4, 4326))).geom as geom
        )
        SELECT
            count(*) AS value,
            width_bucket(valid_from, $3::timestamptz[]) AS time_bin
        FROM contributions c
        JOIN aoi on (ST_Intersects(c.geom, aoi.geom))
        WHERE valid_from >= $1::timestamptz and
              valid_from <  $2::timestamptz
          AND (
              ({filter_where_clause}) or
              -- HACK: ohsome-filter-to-sql does not know about tags before.
              -- Apply same tag filter to tags_before.
              -- In this case all other filter parts are duplicated
              -- and hopefully ignored by query planner.
              ({filter_where_clause.replace("tags", "tags_before")})
          )
        GROUP BY time_bin
        ORDER BY time_bin
    """  # noqa: S608

    records = await db.fetch_rows(
        sql,
        start,
        end,
        series,
        aoi_wkt,
        *filter_args,
    )  # order matters!

    return zerofill_records_to_time_bin_columns(records, series)


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


async def get_features(
    ohsome_filter: OhsomeFilter,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
    clip: bool,
) -> SnapshotColumns:
    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=2)
    sql = f"""
        WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($2, 4326))).geom as geom
        ),
        series AS (
            SELECT unnest($1::timestamptz[]) AS ts
        )
        SELECT
            {aggregation_clause(measure, clip)},
            series.ts AS ts
        FROM contributions c, aoi, series
        WHERE 1=1
            AND ({filter_where_clause})
            -- Global time filter has been part of this query from the beginning on,
            -- but is now disabled because we believe its not necessary.
            -- AND valid_from <= $end::timestamptz
            -- AND valid_to > $start::timestamptz
            AND ST_Intersects(c.geom, aoi.geom)
            -- exclude deleted and invalid states
            AND (status_geom_type).status in ('history', 'latest')
            -- join by timestamp
            AND valid_from <= series.ts
            AND valid_to > series.ts
        GROUP BY series.ts
        ORDER BY series.ts
    """  # noqa: S608
    records = await db.fetch_rows(
        sql,
        series,
        aoi_wkt,
        *filter_args,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_series = {ts: 0 for ts in series}

    for record in records:
        zerofilled_series[record["ts"]] = record["value"]

    timestamps: list[datetime] = list(zerofilled_series.keys())
    values: list[int] = list(zerofilled_series.values())
    return SnapshotColumns(timestamp=timestamps, value=values)


async def get_features_grouped_by_tag(
    ohsome_filter: OhsomeFilter,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
    group_by_tag: str,
    clip: bool,
) -> SnapshotColumnsGrouped:
    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=3)
    limit = CONFIG.group_by_time_series_size_limit
    sql = f"""
        WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($2, 4326))).geom as geom
        ),
        series AS (
            SELECT unnest($1::timestamptz[]) AS ts
        )
        SELECT
            {aggregation_clause(measure, clip)},
            series.ts AS ts,
            tags->>$3 as tag_value
        FROM contributions c, aoi, series
        WHERE 1=1
            AND ({filter_where_clause})
            -- Global time filter has been part of this query from the beginning on,
            -- but is now disabled because we believe its not necessary.
            -- AND valid_from <= $end::timestamptz
            -- AND valid_to > $start::timestamptz
            AND ST_Intersects(c.geom, aoi.geom)
            -- exclude deleted and invalid states
            AND (status_geom_type).status in ('history', 'latest')
            -- join by timestamp
            AND valid_from <= series.ts
            AND valid_to > series.ts
        GROUP BY series.ts, tag_value
        ORDER BY series.ts, tag_value
        LIMIT {limit + 1}
    """  # noqa: S608
    records = await db.fetch_rows(
        sql,
        series,
        aoi_wkt,
        group_by_tag,
        *filter_args,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_totals = {ts: 0 for ts in series}
    all_tags: set[str] = set()

    for record in records:
        zerofilled_totals[record["ts"]] = (
            zerofilled_totals[record["ts"]] + record["value"]
        )
        all_tags.add(record["tag_value"])

    if len(all_tags) * len(series) > limit:
        raise ResultTooLargeError(
            "The provided query produced too many results. The given "
            "time series parameters in combination with the "
            f"group by tags parameter lead to a result larger than {limit} rows."
        )

    zerofilled_results: dict[str, dict[datetime, int]] = dict()
    for tag_value in all_tags:
        zerofilled_results[tag_value] = {ts: 0 for ts in series}
    for record in records:
        zerofilled_results[record["tag_value"]][record["ts"]] = record["value"]

    timestamps: list[datetime] = list(zerofilled_totals.keys())
    total_values: list[int] = list(zerofilled_totals.values())
    group_by_values: dict[str, list[int]] = {
        value: list(x.values())
        for (value, x) in zerofilled_results.items()
        if value is not None
    }
    return SnapshotColumnsGrouped(
        timestamp=timestamps,
        value=total_values,
        values=group_by_values,
    )


def _filter_by_time(
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
) -> tuple[str, list[datetime]]:
    if start == "latest":
        time_args = []
        return (
            """status_geom_type = ANY(array[
           ('latest','Point')::status_geom_type_type,
           ('latest','LineString')::status_geom_type_type,
           ('latest','Polygon')::status_geom_type_type,
           ('latest','MultiPolygon')::status_geom_type_type
           ])
        """,
            time_args,
        )

    if end == "latest":
        time_args = [start]
        if contributions:
            filter_by_time_contributions = """
                AND valid_from >= $2::timestamptz
            """
        else:
            filter_by_time_contributions = """
                AND valid_to    > $2::timestamptz
            """
    else:
        time_args = [start, end]
        if contributions:
            filter_by_time_contributions = """
                AND valid_from >= $2::timestamptz
                AND valid_from  < $3::timestamptz
            """
        else:
            filter_by_time_contributions = """
                AND valid_to    > $2::timestamptz
                AND valid_from <= $3::timestamptz
            """

    return (
        f"""status_geom_type = ANY(array[
       ('latest','Point')::status_geom_type_type,
       ('latest','LineString')::status_geom_type_type,
       ('latest','Polygon')::status_geom_type_type,
       ('latest','MultiPolygon')::status_geom_type_type,
       ('history','Point')::status_geom_type_type,
       ('history','LineString')::status_geom_type_type,
       ('history','Polygon')::status_geom_type_type,
       ('history','MultiPolygon')::status_geom_type_type
       ])
       {filter_by_time_contributions}
    """,
        time_args,
    )


def extract_features(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    start: datetime | Literal["latest"],
    end: datetime | Literal["latest"],
    contributions: bool,
) -> AsyncIterator[list[ExtractionRow]]:
    """Extract all features"""
    if clip:
        clipped_geom_sql = """
        -- is clipped
        CROSS JOIN LATERAL (
          SELECT ST_Covers(aoi.geom, c.geom) as is_covered
        ) cov
        CROSS JOIN LATERAL (
          SELECT
            NOT cov.is_covered AS clipped,
          CASE
            WHEN cov.is_covered THEN c.geom
            ELSE ST_Intersection(c.geom, aoi.geom)
          END AS geom
        ) proc
        """
    else:
        clipped_geom_sql = """
        -- not clipping
        CROSS JOIN LATERAL (
        SELECT
          false as clipped,
          c.geom as geom
        ) proc
        """

    filter_by_time, time_args = _filter_by_time(start, end, contributions)

    filter_where_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter,
        args_shift=len(time_args) + 1,
    )

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
               tags,
               ST_XMin(proc.geom) as xmin,
               ST_YMin(proc.geom) as ymin,
               ST_XMax(proc.geom) as xmax,
               ST_YMax(proc.geom) as ymax,
               (status_geom_type).geom_type as geom_type,
               ST_AsBinary(proc.geom) as geom,
               proc.clipped
        FROM contributions as c
        JOIN aoi ON ST_Intersects(c.geom, aoi.geom)
        {clipped_geom_sql}
        WHERE {filter_by_time}
           AND ({filter_where_clause})
    """  # noqa: S608

    # cast generic asyncpg Record to ExtractionRow
    # TODO: make batch size configurable (maybe as function arg)
    return cast(
        AsyncIterator[list[ExtractionRow]],
        # PERF: batch_size should be different depending on expected row size
        #   (e.g. GeometryType)
        db.fetch_batch(sql, aoi_wkt, *time_args, *filter_args, batch_size=10000),
    )


async def extract_features_collection(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    time: datetime | Literal["latest"],
) -> AsyncIterator[list[ExtractionRow]]:
    """Extract all features"""
    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=2)

    if time == "latest":
        filter_by_time = """status_geom_type = ANY(array[
           ('latest','GeometryCollection')::status_geom_type_type
           ])
           AND 'latest' = $2  -- always true
        """
    else:
        filter_by_time = """status_geom_type = ANY(array[
           ('latest','GeometryCollection')::status_geom_type_type,
           ('history','GeometryCollection')::status_geom_type_type
           ])
           AND valid_from <= $2::timestamptz
           AND valid_to > $2::timestamptz
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
               tags,
               (status_geom_type).geom_type as geom_type,
               ST_AsBinary(c.geom) as geom,
               false as clipped,
               ST_XMin(c.geom) as xmin,
               ST_YMin(c.geom) as ymin,
               ST_XMax(c.geom) as xmax,
               ST_YMax(c.geom) as ymax
        FROM contributions as c
        JOIN aoi ON ST_Intersects(c.geom, aoi.geom)
        WHERE {filter_by_time}
           AND ({filter_where_clause})
    """  # noqa: S608

    # TODO: make batch size configurable (maybe as function arg)
    async for batch in db.fetch_batch(sql, aoi_wkt, time, *filter_args, batch_size=200):
        yield [ExtractionRow(cast(ExtractionRow, item)) for item in batch]


async def extract_features_collection_members_collections(  # noqa: PLR0915
    collections: list[ExtractionRow],
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> list[ExtractionRow]:
    ids = [item["osm_id"] for item in collections]
    versions = [item["osm_version"] for item in collections]

    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=4)

    collections_by_id = {}
    for collection in collections:
        collections_by_id[collection["osm_id"]] = collection
    if time == "latest":
        filter_by_time = """status_geom_type = ANY(array[
           ('latest','Point')::status_geom_type_type,
           ('latest','LineString')::status_geom_type_type,
           ('latest','Polygon')::status_geom_type_type,
           ('latest','MultiPolygon')::status_geom_type_type
           ])
           AND 'latest' = $2  -- always true
        """
    else:
        filter_by_time = """status_geom_type = ANY(array[
           ('latest','Point')::status_geom_type_type,
           ('latest','LineString')::status_geom_type_type,
           ('latest','Polygon')::status_geom_type_type,
           ('latest','MultiPolygon')::status_geom_type_type,
           ('history','Point')::status_geom_type_type,
           ('history','LineString')::status_geom_type_type,
           ('history','Polygon')::status_geom_type_type,
           ('history','MultiPolygon')::status_geom_type_type
           ])
           AND valid_from <= $2::timestamptz
           AND valid_to > $2::timestamptz
        """
    # TODO: ST_Union only for polygon case, otherwise ST_Collect should suffice
    if clip:
        select_geom_sql = (
            "ST_Collect(ST_Intersection(c.geom, aoi.geom)) AS geom, "
            "count(*) FILTER (WHERE NOT ST_Within(c.geom, aoi.geom)) AS clipped_count,"
            "count(*) AS intersects_count "
        )
        join_geom_sql = "JOIN aoi ON ST_Intersects(c.geom, aoi.geom)"
    else:
        select_geom_sql = (
            "ST_Collect(c.geom) AS geom, "
            "0 AS clipped_count, "
            "count(aoi.geom) AS intersects_count "
        )
        join_geom_sql = "LEFT JOIN aoi ON ST_Intersects(c.geom, aoi.geom)"

    sql = f"""
        WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($1, 4326))).geom AS geom
        )
        SELECT
            relation_id,
            geom_type,
            ST_AsBinary(geom) AS geom,
            clipped_count > 0 as clipped,
            intersects_count > 0 as intersects,
            ST_XMin(geom) as xmin,
            ST_YMin(geom) as ymin,
            ST_XMax(geom) as xmax,
            ST_YMax(geom) as ymax
        FROM (
            SELECT collection.id AS relation_id,
                (status_geom_type).geom_type as geom_type,
                {select_geom_sql}
            FROM contributions AS c
            JOIN contributions_members m ON (
                m.member_osm_type = c.osm_type
                AND m.member_osm_id = c.osm_id)
            JOIN unnest($3::int[], $4::int[]) AS collection(id, version) ON (
                collection.id = m.relation_osm_id
                AND collection.version = ANY(m.relation_osm_version_list))
            {join_geom_sql}
            WHERE {filter_by_time} and ({filter_where_clause})
            GROUP BY collection.id, (status_geom_type).geom_type
        )
    """  # noqa: S608
    members = await db.fetch_rows(
        sql,
        aoi_wkt,
        time,
        ids,
        versions,
        *filter_args,
    )
    result: list[ExtractionRow] = []
    for member in members:
        if not clip and not member["intersects"]:
            continue
        item = ExtractionRow(collections_by_id[member["relation_id"]])
        item["geom_type"] = member["geom_type"]
        item["geom"] = member["geom"]
        item["clipped"] = member["clipped"]
        item["xmin"] = member["xmin"]
        item["ymin"] = member["ymin"]
        item["xmax"] = member["xmax"]
        item["ymax"] = member["ymax"]
        result.append(item)
    return result


async def extract_features_collection_members_features(
    collections: list[ExtractionRow],
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[list[ExtractionRow]]:
    filter_where_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=4)

    ids = [item["osm_id"] for item in collections]
    versions = [item["osm_version"] for item in collections]

    if time == "latest":
        filter_by_time = """status_geom_type = ANY(array[
           ('latest','Point')::status_geom_type_type,
           ('latest','LineString')::status_geom_type_type,
           ('latest','Polygon')::status_geom_type_type,
           ('latest','MultiPolygon')::status_geom_type_type
           ])
           AND 'latest' =  $2  -- always true
        """
    else:
        filter_by_time = """status_geom_type = ANY(array[
           ('latest','Point')::status_geom_type_type,
           ('latest','LineString')::status_geom_type_type,
           ('latest','Polygon')::status_geom_type_type,
           ('latest','MultiPolygon')::status_geom_type_type,
           ('history','Point')::status_geom_type_type,
           ('history','LineString')::status_geom_type_type,
           ('history','Polygon')::status_geom_type_type,
           ('history','MultiPolygon')::status_geom_type_type
           ])
           AND valid_from <= $2::timestamptz
           AND valid_to > $2::timestamptz
        """
    if clip:
        clipped_geom_sql = """
        -- is clipped
        JOIN aoi ON ST_Intersects(c.geom, aoi.geom)
        CROSS JOIN LATERAL (
          SELECT ST_Covers(aoi.geom, c.geom) as is_covered
        ) cov
        CROSS JOIN LATERAL (
          SELECT
            NOT cov.is_covered AS clipped,
          CASE
            WHEN cov.is_covered THEN c.geom
            ELSE ST_Intersection(c.geom, aoi.geom)
          END AS geom
        ) proc
        """
    else:
        clipped_geom_sql = """
        -- not clipping
        CROSS JOIN LATERAL (
        SELECT
          false as clipped,
          c.geom as geom
        ) proc
        """

    sql = f"""
        WITH aoi AS (
            SELECT (ST_Dump(ST_GeomFromText($1, 4326))).geom AS geom
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
               tags,
               ST_XMin(proc.geom) as xmin,
               ST_YMin(proc.geom) as ymin,
               ST_XMax(proc.geom) as xmax,
               ST_YMax(proc.geom) as ymax,
               (status_geom_type).geom_type as geom_type,
               ST_AsBinary(proc.geom) as geom,
               proc.clipped,
               m.relation_osm_id as part_of,
               m.member_role as part_of_role,
               m.member_pos_list[array_position(m.relation_osm_version_list , col.version)] as part_of_pos
        FROM unnest($3::int[], $4::int[]) AS col(id, version)
        JOIN contributions_members m ON (
            col.id = m.relation_osm_id
            AND col.version = ANY(m.relation_osm_version_list))
        JOIN contributions AS c ON (
            m.member_osm_type = c.osm_type
            AND m.member_osm_id = c.osm_id)
        {clipped_geom_sql}
        WHERE {filter_by_time}
          AND ({filter_where_clause})
    """  # noqa: E501, S608

    async for batch in db.fetch_batch(
        sql, aoi_wkt, time, ids, versions, *filter_args, batch_size=10000
    ):
        yield [ExtractionRow(cast(ExtractionRow, item)) for item in batch]


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
