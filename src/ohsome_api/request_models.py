# TODO: return request params in response?
from datetime import datetime, timedelta, timezone
from enum import StrEnum
from typing import Literal, Self

from geojson_pydantic import MultiPolygon, Polygon
from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    computed_field,
    field_validator,
    model_validator,
)
from pydantic.alias_generators import to_camel

td_adapter = TypeAdapter(timedelta)


class RequestConfigModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
    )


class MeasureRequestModel(StrEnum):
    COUNT = "count"
    LENGTH = "length"
    AREA = "area"


class FilterRequestModel(RequestConfigModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        description=(
            "[filter language documentation]"
            "(https://docs.ohsome.org/ohsome-api/v1/filter.html)"
        ),
        json_schema_extra={"example": "type:node and natural=tree"},
    )


class AoiRequestModel(RequestConfigModel):
    aoi: Polygon | MultiPolygon = Field(
        description="Area of interest as a GeoJSON Geometry (Polygon or MultiPolygon).",
        json_schema_extra={
            "examples": [
                {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [8.72362, 49.41582],
                            [8.68812, 49.41582],
                            [8.68812, 49.40390],
                            [8.72362, 49.40390],
                            [8.72362, 49.41582],
                        ]
                    ],
                },
            ]
        },
    )

    @computed_field()
    @property
    def aoi_wkt(self) -> str:
        return self.aoi.wkt


class TimeRequestModel(RequestConfigModel):
    start: datetime | Literal["earliest"] = Field(
        description=(
            "Start timestamp (ISO-8601, UTC). "
            "Earliest timestamp is 2007-10-08T00:00:00Z. "
            "As shorthand 'earliest' can be used instead of a timestamp."
        ),
        json_schema_extra={"examples": ["2026-01-01T00:00:00Z", "earliest"]},
    )
    end: datetime = Field(
        description="End timestamp (ISO-8601, UTC)",
        json_schema_extra={"examples": ["2026-04-17T00:00:00Z"]},
    )

    @field_validator("start", "end", mode="before")
    @classmethod
    def transform_literal_to_timestamp(
        cls,
        value: datetime | Literal["earliest"],
    ) -> datetime:
        if value == "earliest":
            return datetime(2007, 10, 8, tzinfo=timezone.utc)

        return value

    @field_validator("start", "end")
    @classmethod
    def validate_timezone(cls, value: datetime) -> datetime:
        # Allow only UTC.
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)

        if value.tzinfo == timezone.utc:
            return value

        raise ValueError("Only UTC timestamps are supported.")

    @field_validator("start", mode="after")
    @classmethod
    def validate_start(cls, value: datetime) -> datetime:
        if value >= datetime(2007, 10, 8, tzinfo=timezone.utc):
            return value

        raise ValueError("Start needs to be greater or equal then 2007-10-08.")

    @model_validator(mode="after")
    def validate_end_greater_than_start(self) -> Self:
        assert isinstance(self.start, datetime)  # noqa: S101

        if self.end > self.start:
            return self

        raise ValueError("End timestamp needs to be greater than start timestamp.")


class TimeBinsRequestModel(TimeRequestModel):
    bin_size: str | None = Field(
        default=None,
        description="Bin size (ISO-8601 duration).",
        json_schema_extra={"example": "P1M"},
    )

    @field_validator("bin_size")
    @classmethod
    def validate_bin_size(cls, value: str | None) -> str | None:
        if value is not None:
            # uses Pydantic internal logic to validate as timedelta
            td_adapter.validate_python(value)
        return value


class TimeSeriesRequestModel(TimeRequestModel):
    interval: str | None = Field(
        default=None,
        description="Interval (ISO-8601 duration).",
        json_schema_extra={"example": "P1M"},
    )

    @field_validator("interval")
    @classmethod
    def validate_interval(cls, value: str | None) -> str | None:
        if value is not None:
            # uses Pydantic internal logic to validate as timedelta
            td_adapter.validate_python(value)
        return value


class RequestParametersModel(AoiRequestModel, FilterRequestModel):
    pass


class TimeBinsRequestParametersModel(AoiRequestModel, FilterRequestModel):
    time_bins: TimeBinsRequestModel = Field(
        description=(
            "Time bins defined using a start/end timestamp (ISO-8601, UTC) "
            "and a bin size (ISO-8601 duration). Last bin might not cover bin size."
        )
    )


class TimeSeriesRequestParametersModel(AoiRequestModel, FilterRequestModel):
    time_series: TimeSeriesRequestModel = Field(
        description=(
            "Time series defined using a start/end timestamp (ISO-8601, UTC) "
            "and a interval (ISO-8601 duration). "
            "The interval between the last two timestamp might not fit given duration."
        )
    )
