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
SQL_QUERY_TEMPLATE_MEMBERS_COLLECTIONS = Path(
    Path(__file__).parent / "features_collection_members_collections.sql"
).read_text()
SQL_QUERY_TEMPLATE_MEMBERS_FEATURES = Path(
    Path(__file__).parent / "features_collection_members_features.sql"
).read_text()


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
        """
        time_args = []
    else:
        time_clause = """
            status_geom_type = ANY(array[
               ('latest','GeometryCollection')::status_geom_type_type,
               ('history','GeometryCollection')::status_geom_type_type
            ])
            AND valid_from <= $2::timestamptz
            AND valid_to > $2::timestamptz
        """
        time_args = [time]

    filter_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter,
        args_shift=len(time_args) + 1,
    )

    sql = SQL_QUERY_TEMPLATE % {
        "time_clause": time_clause,
        "filter_clause": filter_clause,
    }

    # TODO: make batch size configurable (maybe as function arg)
    async for batch in db.fetch_batch(
        sql, aoi_wkt, *time_args, *filter_args, batch_size=200
    ):
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

    collections_by_id = {}
    for collection in collections:
        collections_by_id[collection["osm_id"]] = collection

    if time == "latest":
        time_clause = """
            status_geom_type = ANY(array[
               ('latest','Point')::status_geom_type_type,
               ('latest','LineString')::status_geom_type_type,
               ('latest','Polygon')::status_geom_type_type,
               ('latest','MultiPolygon')::status_geom_type_type
            ])
        """
        time_args = []
    else:
        time_clause = """
            status_geom_type = ANY(array[
               ('latest','Point')::status_geom_type_type,
               ('latest','LineString')::status_geom_type_type,
               ('latest','Polygon')::status_geom_type_type,
               ('latest','MultiPolygon')::status_geom_type_type,
               ('history','Point')::status_geom_type_type,
               ('history','LineString')::status_geom_type_type,
               ('history','Polygon')::status_geom_type_type,
               ('history','MultiPolygon')::status_geom_type_type
            ])
            AND valid_from <= $4::timestamptz
            AND valid_to > $4::timestamptz
        """
        time_args = [time]

    filter_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter,
        args_shift=len(time_args) + 3,
    )

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

    sql = SQL_QUERY_TEMPLATE_MEMBERS_COLLECTIONS % {
        "select_geom_sql": select_geom_sql,
        "join_geom_sql": join_geom_sql,
        "time_clause": time_clause,
        "filter_clause": filter_clause,
    }
    members = await db.fetch_rows(
        sql,
        aoi_wkt,
        ids,
        versions,
        *time_args,
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

    ids = [item["osm_id"] for item in collections]
    versions = [item["osm_version"] for item in collections]

    if time == "latest":
        time_clause = """status_geom_type = ANY(array[
           ('latest','Point')::status_geom_type_type,
           ('latest','LineString')::status_geom_type_type,
           ('latest','Polygon')::status_geom_type_type,
           ('latest','MultiPolygon')::status_geom_type_type
           ])
        """
        time_args = []
    else:
        time_clause = """status_geom_type = ANY(array[
           ('latest','Point')::status_geom_type_type,
           ('latest','LineString')::status_geom_type_type,
           ('latest','Polygon')::status_geom_type_type,
           ('latest','MultiPolygon')::status_geom_type_type,
           ('history','Point')::status_geom_type_type,
           ('history','LineString')::status_geom_type_type,
           ('history','Polygon')::status_geom_type_type,
           ('history','MultiPolygon')::status_geom_type_type
           ])
           AND valid_from <= $4::timestamptz
           AND valid_to > $4::timestamptz
        """
        time_args = [time]

    filter_clause, filter_args = ohsome_filter_to_sql(
        ohsome_filter,
        args_shift=len(time_args) + 3,
    )

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

    sql = SQL_QUERY_TEMPLATE_MEMBERS_FEATURES % {
        "clipped_geom_sql": clipped_geom_sql,
        "time_clause": time_clause,
        "filter_clause": filter_clause,
    }
    async for batch in db.fetch_batch(
        sql, aoi_wkt, ids, versions, *time_args, *filter_args, batch_size=10000
    ):
        yield [ExtractionRow(cast(ExtractionRow, item)) for item in batch]
