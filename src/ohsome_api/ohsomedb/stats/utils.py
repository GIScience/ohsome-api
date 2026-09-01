from ohsome_api.models import (
    MeasureEnum,
)


class TimeSeriesTooLargeError(ValueError):
    pass


class ResultTooLargeError(ValueError):
    pass


def get_aggregation_clause(measure: MeasureEnum, clip: bool) -> str:
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
