from datetime import datetime
from typing import Literal

from ohsome_api.models import Measure


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


def get_aggregation_clause(measure: Measure | Literal["user"], clip: bool) -> str:  # noqa: C901
    match measure:
        case "count":
            return "COUNT(*) AS value"
        case "user":
            return "COUNT(DISTINCT user_id) AS value"
        case "length":
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
        case "area":
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
