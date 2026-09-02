# TODO: Factor out SQLs to files
from datetime import datetime
from pathlib import Path
from typing import AsyncIterator, Literal, cast

from ohsome_filter_to_sql import OhsomeFilter, ohsome_filter_to_sql

from ohsome_api.db.db import db
from ohsome_api.models import (
    ExtractionRow,
)

SQL_QUERY_TEMPLATE = Path(Path(__file__).parent / "features_collection.sql").read_text()


async def extract_features_collection(
    ohsome_filter: OhsomeFilter,
    aoi_wkt: str,
    time: datetime | Literal["latest"],
) -> AsyncIterator[list[ExtractionRow]]:
    """Extract all features"""

    if time == "latest":
        time_clause = """
            status_geom_type = ANY(array[
                ('latest','GeometryCollection')::status_geom_type_type
            ])
            AND 'latest' = $2  -- always true
        """
    else:
        time_clause = """
            status_geom_type = ANY(array[
               ('latest','GeometryCollection')::status_geom_type_type,
               ('history','GeometryCollection')::status_geom_type_type
               ])
            AND valid_from <= $2::timestamptz
            AND valid_to > $2::timestamptz
        """

    filter_clause, filter_args = ohsome_filter_to_sql(ohsome_filter, args_shift=2)

    sql = SQL_QUERY_TEMPLATE % {
        "time_clause": time_clause,
        "filter_clause": filter_clause,
    }

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
