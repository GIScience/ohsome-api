from datetime import datetime

from asyncpg import Record

from ohsome_api.models import (
    Measure,
    TimeBinsResult,
)


class TimeSeriesTooLargeError(ValueError):
    pass


class ResultTooLargeError(ValueError):
    pass


def zerofill_records_to_time_bin_columns(
    records: list[Record],
    series: list[datetime],
) -> TimeBinsResult:
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

    return TimeBinsResult(start=start_timestamps, end=end_timestamps, value=values)


def get_aggregation_clause(measure: Measure, clip: bool) -> str:
    match measure:
        case "count":
            return "COUNT(*) AS value"
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
