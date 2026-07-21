from datetime import datetime
from typing import AsyncIterator, Literal, cast

from asyncpg import Record

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.models import (
    ExtractionRow,
    MeasureEnum,
    SnapshotColumns,
    TimeBinColumns,
)

SCHEMA = CONFIG.ohsomedb.schemaname


class TimeSeriesTooLargeError(ValueError):
    pass


async def generate_timestamp_series(
    start: datetime,
    end: datetime,
    interval: str | None,
) -> list[datetime]:
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
            "The provided values for the timeBin or timeSeries parameter "
            f"lead to a time series larger than {limit} points/bins."
        )

    # TODO: Extract post-processing to own function and write unit-tests
    results = [r["ts"] for r in records]
    if results[-1] != end:
        # include uneven time bin
        results.append(end)
    return results


async def get_latest_timestamp() -> datetime:
    sql = f'SELECT last_timestamp FROM "{SCHEMA}".contributions_state'  # noqa: S608
    return (await db.fetch_row(sql))[0]


async def get_metadata() -> dict[str, datetime]:
    sql = f"""
        WITH
            first AS (
                SELECT value::timestamptz as first_timestamp
                FROM "{SCHEMA}".ohsomedb_metadata
                WHERE key = 'valid_from'
            ), last AS (
                SELECT last_timestamp
                FROM "{SCHEMA}".contributions_state
            )
        SELECT first_timestamp as start, last_timestamp as end FROM first, last;
"""  # noqa: S608
    return dict(await db.fetch_row(sql))


async def get_currentness(
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
) -> TimeBinColumns:
    filter_args_count = len(filter_args)
    match measure:
        case MeasureEnum.COUNT:
            aggregation_clause = "COUNT(*) AS value"
        case MeasureEnum.LENGTH:
            # [m]
            aggregation_clause = """
            ROUND(
                SUM(
                    CASE
                        WHEN ST_Within(
                            c.geom,
                            aoi.geom
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
            aggregation_clause = """
            ROUND(
                SUM(
                    CASE
                        WHEN ST_Within(
                            c.geom,
                            aoi.geom
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
    sql = f"""
        WITH aoi AS (
            SELECT ST_GeomFromText(${filter_args_count + 4}, 4326) as geom
        )
        SELECT
            {aggregation_clause},
            width_bucket(valid_from, ${filter_args_count + 3}::timestamptz[]) AS time_bin
        FROM "{SCHEMA}".contributions c, aoi
        WHERE ({filter_where_clause})
        AND valid_from BETWEEN ${filter_args_count + 1}::timestamptz
                           AND ${filter_args_count + 2}::timestamptz
        AND ST_Intersects(c.geom, aoi.geom)
        AND (status_geom_type).status = 'latest'
        GROUP BY time_bin
        ORDER BY time_bin
    """  # noqa: S608, E501
    records = await db.fetch_rows(
        sql,
        *filter_args,
        start,
        end,
        series,
        aoi_wkt,
    )  # order matters!

    return zerofill_records_to_time_bin_columns(records, series)


async def get_contributors_activity(
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
) -> TimeBinColumns:
    filter_args_count = len(filter_args)
    sql = f"""
        WITH aoi AS (
            SELECT ST_GeomFromText(${filter_args_count + 4}, 4326) as geom
        )
        SELECT
            count(distinct user_id) AS value,
            width_bucket(valid_from, ${filter_args_count + 3}::timestamptz[]) AS time_bin
        FROM "{SCHEMA}".contributions c, aoi
        WHERE ({filter_where_clause})
        AND valid_from BETWEEN ${filter_args_count + 1}::timestamptz
                           AND ${filter_args_count + 2}::timestamptz
        AND ST_Intersects(c.geom, aoi.geom)
        GROUP BY time_bin
        ORDER BY time_bin
    """  # noqa: S608, E501

    records = await db.fetch_rows(
        sql,
        *filter_args,
        start,
        end,
        series,
        aoi_wkt,
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


async def get_features(
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
    measure: MeasureEnum,
) -> SnapshotColumns:
    filter_args_count = len(filter_args)
    match measure:
        case MeasureEnum.COUNT:
            aggregation_clause = "COUNT(*) AS value"
        case MeasureEnum.LENGTH:
            # [m]
            aggregation_clause = """
            ROUND(
                SUM(
                    CASE
                        WHEN ST_Within(
                            c.geom,
                            aoi.geom
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
            aggregation_clause = """
            ROUND(
                SUM(
                    CASE
                        WHEN ST_Within(
                            c.geom,
                            aoi.geom
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
    sql = f"""
        WITH aoi AS (
            SELECT ST_GeomFromText(${filter_args_count + 4}, 4326) as geom
        ),
        series AS (
            SELECT unnest(${filter_args_count + 3}::timestamptz[]) AS ts
        )
        SELECT
            {aggregation_clause},
            series.ts AS ts
        FROM "{SCHEMA}".contributions c, aoi, series
        WHERE 1=1
            AND ({filter_where_clause})
            -- global time filter
            AND valid_from <= ${filter_args_count + 2}::timestamptz
            AND valid_to > ${filter_args_count + 1}::timestamptz
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
        *filter_args,
        start,
        end,
        series,
        aoi_wkt,
    )  # order matters!

    # TODO: extract post-processing to function
    zerofilled_series = {ts: 0 for ts in series}

    for record in records:
        zerofilled_series[record["ts"]] = record["value"]

    timestamps: list[datetime] = list(zerofilled_series.keys())
    values: list[int] = list(zerofilled_series.values())
    return SnapshotColumns(timestamp=timestamps, value=values)


def extract_features(
    filter_where_clause: str,
    filter_args: tuple,
    aoi_wkt: str,
    clip: bool,
    time: datetime | Literal["latest"],
) -> AsyncIterator[list[ExtractionRow]]:
    filter_args_count = len(filter_args)
    if clip:
        select_geom_sql = (
            "ST_AsBinary(ST_Intersection(c.geom, aoi.geom)) as geom, "
            "NOT ST_Within( c.geom, aoi.geom ) as clipped "
        )
    else:
        select_geom_sql = "ST_AsBinary(c.geom) as geom, false as clipped "

    if time == "latest":
        filter_by_time = f"""status_geom_type = ANY(array[
           ('latest','Point')::"{SCHEMA}".status_geom_type_type,
           ('latest','LineString')::"{SCHEMA}".status_geom_type_type,
           ('latest','Polygon')::"{SCHEMA}".status_geom_type_type,
           ('latest','MultiPolygon')::"{SCHEMA}".status_geom_type_type,
           ('latest','GeometryCollection')::"{SCHEMA}".status_geom_type_type
           ])
           AND 'latest' = ${filter_args_count + 2}  -- always true
        """
    else:
        filter_by_time = f"""status_geom_type = ANY(array[
           ('latest','Point')::"{SCHEMA}".status_geom_type_type,
           ('latest','LineString')::"{SCHEMA}".status_geom_type_type,
           ('latest','Polygon')::"{SCHEMA}".status_geom_type_type,
           ('latest','MultiPolygon')::"{SCHEMA}".status_geom_type_type,
           ('latest', 'GeometryCollection')::"{SCHEMA}".status_geom_type_type,
           ('history','Point')::"{SCHEMA}".status_geom_type_type,
           ('history','LineString')::"{SCHEMA}".status_geom_type_type,
           ('history','Polygon')::"{SCHEMA}".status_geom_type_type,
           ('history','MultiPolygon')::"{SCHEMA}".status_geom_type_type,
           ('history','GeometryCollection')::"{SCHEMA}".status_geom_type_type
           ])
           AND valid_from <= ${filter_args_count + 2}::timestamptz
           AND valid_to > ${filter_args_count + 2}::timestamptz
        """

    sql = f"""
        WITH aoi AS (
            SELECT ST_GeomFromText(${filter_args_count + 1}, 4326) as geom
        )
        SELECT osm_type,
               osm_id,
               valid_from,
               osm_version,
               osm_minor_version,
               osm_edits,
               user_id,
               user_name,
               changeset_id,
               tags,
               ST_XMin(c.geom) as xmin,
               ST_YMin(c.geom) as ymin,
               ST_XMax(c.geom) as xmax,
               ST_YMax(c.geom) as ymax,
               {select_geom_sql}
        FROM "{SCHEMA}".contributions as c, aoi
        WHERE {filter_by_time}
           AND ST_Intersects(c.geom, aoi.geom)
           AND ({filter_where_clause})
    """  # noqa: S608
    # cast generic asyncpg Record to ExtractionRow
    # TODO: make batch size configurable (maybe as function arg)
    return cast(
        AsyncIterator[list[ExtractionRow]],
        # PERF: batch_size should be different depending on expected row size
        #   (e.g. GeometryType)
        db.fetch_batch(sql, *filter_args, aoi_wkt, time, batch_size=10000),
    )
