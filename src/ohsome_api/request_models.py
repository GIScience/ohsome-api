# TODO: return request params in response?
import logging
from datetime import datetime, timedelta, timezone
from enum import StrEnum
from typing import Self

from geojson_pydantic import Feature, FeatureCollection, MultiPolygon, Polygon
from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    field_validator,
    model_validator,
)
from pydantic.alias_generators import to_camel

logger = logging.getLogger(__name__)

td_adapter = TypeAdapter(timedelta)


class BaseRequestModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
    )


class Measure(StrEnum):
    COUNT = "count"
    LENGTH = "length"
    AREA = "area"


class GeoJsonFeature(Feature):
    geometry: Polygon | MultiPolygon
    properties: dict | None


class GeoJsonFeatureCollection(FeatureCollection[GeoJsonFeature]):
    features: list[GeoJsonFeature] = Field(..., min_length=1)

    model_config = ConfigDict(
        json_schema_extra={
            "examples": [
                {
                    "type": "FeatureCollection",
                    "features": [
                        {
                            "type": "Feature",
                            "properties": {},
                            "geometry": {
                                "coordinates": [
                                    [
                                        [8.72362, 49.41582],
                                        [8.68812, 49.41582],
                                        [8.68812, 49.40390],
                                        [8.72362, 49.40390],
                                        [8.72362, 49.41582],
                                    ]
                                ],
                                "type": "Polygon",
                            },
                        }
                    ],
                }
            ]
        }
    )


class BaseTime(BaseRequestModel):
    start: datetime = Field(
        example="2026-01-01T00:00:00Z",
        description="""
            Only UTC timestamps are supported.
            Earliest OSM timestamp is 2007-10-08T00:00:00Z.
            """,
        ge=datetime(2007, 10, 8),
    )
    end: datetime = Field(
        example="2026-04-17T00:00:00Z",
        description="Only UTC timestamps are supported.",
    )

    @model_validator(mode="after")
    def validate_end_greater_than_start(self) -> Self:
        if self.end > self.start:
            return self

        raise ValueError("End timestamp needs to be greater than start timestamp.")

    @field_validator("start", "end")
    @classmethod
    def validate_timezone(cls, value: datetime) -> datetime:
        """Allow only UTC."""
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)

        if value.tzinfo == timezone.utc:
            return value

        raise ValueError("Only UTC timestamps are supported.")


class TimeBins(BaseTime):
    bin_size: str | None = Field(example="P1M", default=None)

    @field_validator("bin_size")
    @classmethod
    def validate_bin_size(cls, value: str) -> str:
        # uses Pydantic internal logic to validate as timedelta
        td_adapter.validate_python(value)
        return value


class TimeSeries(BaseTime):
    interval: str | None = Field(example="P1M", default=None)

    @field_validator("interval")
    @classmethod
    def validate_interval(cls, value: str) -> str:
        # uses Pydantic internal logic to validate as timedelta
        td_adapter.validate_python(value)
        return value


class BaseParameters(BaseRequestModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        description="""[filter language documentation](
        https://docs.ohsome.org/ohsome-api/v1/filter.html)""",
        example="type:node and natural=tree",
    )
    aoi: GeoJsonFeatureCollection = Field(
        description="""Area of interest as a GeoJSON
        FeatureCollection. Only Polygon and MultiPolygon
        geometry types are allowed. If more than one Feature
        are supplied, the result will include results for each
        Feature separately.
        """,
    )


class TimeBinsParameters(BaseParameters):
    time_bins: TimeBins = Field(
        description="""[time documentation](
        https://docs.ohsome.org/ohsome-api/v1/time.html)""",
    )


class TimeSeriesParameters(BaseParameters):
    time_series: TimeSeries = Field(
        description="""[time documentation](
        https://docs.ohsome.org/ohsome-api/v1/time.html)""",
    )
