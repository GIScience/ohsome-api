# TODO: return request params in response?
import json
from datetime import datetime, timedelta, timezone
from enum import StrEnum
from typing import Annotated, Any, Literal, Self

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
from shapely.geometry import mapping
from shapely.wkt import loads as load_wkt

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


BBox = Annotated[
    tuple[float, float, float, float],
    Field(
        title="BoundingBox",
        description="Bounding Box (xmin, ymin, xmax, ymax)",
        json_schema_extra={"example": (8.68812, 49.40390, 8.72362, 49.41582)},
    ),
]

WKT = Annotated[
    str,
    Field(
        title="WKT",
        description="Well-Known Text (Polygon or MultiPolygon)",
        json_schema_extra={
            "example": "POLYGON ((8.72362 49.41582,8.68812 49.41582,8.68812 49.4039,8.72362 49.4039,8.72362 49.41582))"  # noqa: E501
        },
    ),
]

GeoJSONGeometry = Annotated[
    Polygon | MultiPolygon,
    Field(
        title="GeoJSON Polygon or MultiPolygon",
        json_schema_extra={
            "example": {
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
        },
    ),
]


class AoiRequestModel(RequestConfigModel):
    aoi: GeoJSONGeometry | BBox | WKT = Field(
        description=(
            "Area of interest as a GeoJSON Geometry, Bounding Box or WKT. "
            "As geometry only Polygon or MultiPolygon are allowed."
        ),
    )

    @computed_field()
    @property
    def aoi_wkt(self) -> str:
        return self.aoi.wkt  # type: ignore

    @classmethod
    def bbox(cls, value: BBox) -> Polygon | MultiPolygon:  # noqa: C901
        xmin, ymin, xmax, ymax = value
        if xmin >= xmax or ymin >= ymax:
            raise ValueError("min coordinate need to be smaller than max coordinate.")
        if ymin < -90 or ymax > 90:
            raise ValueError("y coordinate need to be between -90 and 90.")
        if xmin <= -360 or xmax >= 360:
            raise ValueError("x coordinate need to be between -360 and 360.")
        if (xmax - xmin) >= 360:
            xmin, xmax = -180.0, 180.0
        if xmin >= -180 and xmax <= 180:
            return Polygon.from_bounds(xmin, ymin, xmax, ymax)

        polys = []
        if xmin < -180:
            polys.append(Polygon.from_bounds(xmin + 360, ymin, 180, ymax).coordinates)
            polys.append(Polygon.from_bounds(-180, ymin, xmax, ymax).coordinates)
        if xmax > 180:
            polys.append(Polygon.from_bounds(xmin, ymin, 180, ymax).coordinates)
            polys.append(Polygon.from_bounds(-180, ymin, xmax - 360, ymax).coordinates)

        return MultiPolygon(coordinates=polys, type="MultiPolygon")

    @field_validator("aoi", mode="before")
    @classmethod
    def parse_string_input(cls, value: Any) -> Any:  # noqa: ANN401
        if not isinstance(value, str):
            return value

        value = value.strip()
        # 1. Handle GeoJSON String (from GET parameters)
        if (value.startswith("{") and value.endswith("}")) or (
            value.startswith("[") and value.endswith("]")
        ):
            try:
                return json.loads(value)
            except Exception as e:
                raise ValueError(f"Invalid JSON string: {e}") from e

        return cls.try_transform_wkt_str_to_geojson(value)

    @classmethod
    def try_transform_wkt_str_to_geojson(cls, value: str) -> dict:
        try:
            return mapping(load_wkt(value))
        except Exception as e:
            raise ValueError(f"Input string is invalid WKT: {e}") from e

    @field_validator("aoi")
    @classmethod
    def transform_bbox_to_geojson(
        cls,
        value: Polygon | MultiPolygon | BBox,
    ) -> Polygon | MultiPolygon:
        if isinstance(value, Polygon | MultiPolygon):
            return value
        return cls.bbox(value)


class TimeRequestModel(RequestConfigModel):
    start: datetime | Literal["earliest"] = Field(
        description=(
            "Start timestamp (ISO-8601, UTC). "
            "Earliest timestamp is 2007-10-08T00:00:00Z. "
            "As shorthand 'earliest' can be used instead of a timestamp."
        ),
        json_schema_extra={"examples": ["2026-01-01T00:00:00Z", "earliest"]},
    )
    end: datetime | Literal["latest"] = Field(
        description=(
            "End timestamp (ISO-8601, UTC). "
            "To include the most recent data "
            "'latest' can be used instead of a timestamp."
        ),
        json_schema_extra={"examples": ["2026-04-17T00:00:00Z"]},
    )

    @field_validator("start", mode="before")
    @classmethod
    def transform_earliest_to_timestamp(
        cls,
        value: datetime | Literal["earliest"],
    ) -> datetime:
        if value == "earliest":
            return datetime(2007, 10, 8, tzinfo=timezone.utc)

        return value

    @field_validator("start", "end")
    @classmethod
    def validate_timezone(
        cls, value: datetime | Literal["latest"]
    ) -> datetime | Literal["latest"]:
        if value == "latest":
            return value

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

        if self.end == "latest":
            return self

        if self.end > self.start:
            return self

        raise ValueError("End timestamp needs to be greater than start timestamp.")


class TimeBinSizeRequestModel(TimeRequestModel):
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


class TimeIntervalRequestModel(TimeRequestModel):
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


class TimeSeriesRequestModel(RequestConfigModel):
    time_series: TimeIntervalRequestModel = Field(
        description=(
            "Time series defined using a start/end timestamp (ISO-8601, UTC) "
            "and a interval (ISO-8601 duration). "
            "The interval between the last two timestamp might not fit given duration."
        )
    )


class TimeBinsRequestModel(RequestConfigModel):
    time_bins: TimeBinSizeRequestModel = Field(
        description=(
            "Time bins defined using a start/end timestamp (ISO-8601, UTC) "
            "and a bin size (ISO-8601 duration). Last bin might not cover bin size."
        )
    )


class ExtractionRequestModel(RequestConfigModel):
    clip: bool = Field(
        default=True, description="Whether to clip extracted features with AOI or not."
    )
    timestamp: datetime | Literal["latest"] = Field(
        default="latest",
        description=(
            "Extraction timestamp (ISO-8601, UTC). "
            "For the most recent data "
            "'latest' can be used instead of a timestamp."
        ),
        json_schema_extra={"examples": ["latest", "2026-04-17T00:00:00Z"]},
    )

    @field_validator("timestamp")
    @classmethod
    def validate_timezone(
        cls, value: datetime | Literal["latest"]
    ) -> datetime | Literal["latest"]:
        if value == "latest":
            return value

        # Allow only UTC.
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)

        if value.tzinfo == timezone.utc:
            return value

        raise ValueError("Only UTC timestamps are supported.")

    @field_validator("timestamp")
    @classmethod
    def validate_timestamp(
        cls,
        value: datetime | Literal["latest"],
    ) -> datetime | Literal["latest"]:
        if value == "latest":
            return value

        if value >= datetime(2007, 10, 8, tzinfo=timezone.utc):
            return value

        raise ValueError("Time needs to be greater or equal then 2007-10-08.")


class ExtractionRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
    ExtractionRequestModel,
):
    pass


class TimeBinsRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
    TimeBinsRequestModel,
):
    pass


class TimeSeriesRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
    TimeSeriesRequestModel,
):
    pass
