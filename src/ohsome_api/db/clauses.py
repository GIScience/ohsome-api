from datetime import datetime
from typing import Literal


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
