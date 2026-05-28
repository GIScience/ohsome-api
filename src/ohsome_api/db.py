from datetime import datetime

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.models import FeaturesRowModel, TimeBinsRowModel
from ohsome_api.request_models import Measure

SCHEMA = CONFIG.ohsomedb.schemaname


async def generate_timestamp_series(
    start: datetime,
    end: datetime,
    interval: str | None,
    limit: int = 10_000,
) -> list[datetime]:
    if interval is None:
        return [start, end]

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
        # TODO: Use custom exception and handle it in fastapi
        # TODO: Write API integration test to check if error gets to user
        # TODO: Add limitation to docs
        raise ValueError(
            "Time parameters including bin_size lead to "
            f"a time series larger than {limit} bins."
        )

    # TODO: Extract post-processing to own function and write unit-tests
    results = [r["ts"] for r in records]
    if results[-1] != end:
        # include uneven time bin
        results.append(end)
    return results


async def get_latest_timestamp() -> datetime:
    sql = f'SELECT last_timestamp FROM "{SCHEMA}".contributions_state'  # noqa: S608
    record = await db.fetch_row(sql)
    if not isinstance(record["last_timestamp"], datetime):
        raise TypeError()
    return record["last_timestamp"]


# TODO: decide what to do about too many arguments linter
# TODO: fix complexity lint warning
async def get_currentness(  # noqa: C901, PLR0913
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
    measure: Measure,
) -> list[TimeBinsRowModel]:
    filter_args_count = len(filter_args)
    match measure:
        case Measure.COUNT:
            aggregation_clause = "COUNT(*) AS value"
        case Measure.LENGTH:
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
        case Measure.AREA:
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

    # TODO: extract post-processing to function
    zerofilled_series = {i: 0 for i in range(len(series) - 1)}

    for record in records:
        zerofilled_series[record["time_bin"] - 1] = record["value"]

    return [
        TimeBinsRowModel(
            value=count,
            start=series[time_bin],
            end=series[time_bin + 1],
        )
        for time_bin, count in zerofilled_series.items()
    ]


async def get_users_activity(  # noqa: PLR0913
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
) -> list[TimeBinsRowModel]:
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

    # TODO: extract post-processing to function
    zerofilled_series = {i: 0 for i in range(len(series) - 1)}

    for record in records:
        zerofilled_series[record["time_bin"] - 1] = record["value"]

    return [
        TimeBinsRowModel(
            value=count,
            start=series[time_bin],
            end=series[time_bin + 1],
        )
        for time_bin, count in zerofilled_series.items()
    ]


# TODO: decide what to do about too many arguments linter
# TODO: fix complexity lint warning
async def get_features(  # noqa: C901, PLR0913
    filter_where_clause: str,
    filter_args: tuple,
    start: datetime,
    end: datetime,
    series: list[datetime],
    aoi_wkt: str,
    measure: Measure,
) -> list[FeaturesRowModel]:
    filter_args_count = len(filter_args)
    match measure:
        case Measure.COUNT:
            aggregation_clause = "COUNT(*) AS value"
        case Measure.LENGTH:
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
        case Measure.AREA:
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

    return [
        FeaturesRowModel(value=value, timestamp=ts)
        for ts, value in zerofilled_series.items()
    ]
