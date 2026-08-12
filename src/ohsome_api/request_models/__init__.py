from datetime import datetime, timedelta, timezone
from enum import StrEnum
from typing import Literal, Optional

from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    computed_field,
    field_validator,
)
from pydantic.alias_generators import to_camel

from ohsome_api.config import CONFIG
from ohsome_api.request_models.aoi import AoiQueryModel, AoiRequestModel
from ohsome_api.request_models.time import (
    TimeBinsRequestModel,
    TimeRangeRequestModel,
    TimeSeriesRequestModel,
    Timestamp,
    TimestampLatest,
)

td_adapter = TypeAdapter(timedelta)


class BoundingBoxValidationError(ValueError):
    pass


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
            "Filter for OSM data. "
            "Please refer to the [ohsome filter language documentation]"
            f"({CONFIG.external_docs_url}/reference.html#filter)"
        ),
        json_schema_extra={"example": "type:node and natural=tree"},
    )


class CollectionsFilterRequestModel(RequestConfigModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        description=(
            "Filter for OSM data. "
            "Please refer to the [ohsome filter language documentation]"
            f"({CONFIG.external_docs_url}/reference.html#filter)"
        ),
        json_schema_extra={
            "example": "type:relation and type=route and route=bus and service=night"
        },
    )


class CollectionsMemberFilterRequestModel(RequestConfigModel):
    member_filter: OhsomeFilter = Field(
        alias="member_filter",
        description=(
            "Filter for OSM data. "
            "Please refer to the [ohsome filter language documentation]"
            f"({CONFIG.external_docs_url}/reference.html#filter)"
        ),
        json_schema_extra={"example": "geometry:line"},
        default="*",
    )


class GroupByTagModel(BaseModel):
    type: Literal["byTag"]
    key: str


class GroupByRequestModel(RequestConfigModel):
    group_by: Optional[GroupByTagModel] = Field(
        description=(
            "(experimental, optional), if given indicates that the "
            "results should also values for individual subsets of the "
            "result defined by the presence of tags with the given key"
        ),
        json_schema_extra={
            "examples": [None],
        },
        default=None,
    )


class ExtractionQueryModel(RequestConfigModel):
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

    @computed_field
    @property
    def timestamp_start(self) -> datetime | Literal["earliest", "latest"]:
        return self.timestamp

    @computed_field
    @property
    def timestamp_end(self) -> datetime | Literal["latest"]:
        return self.timestamp

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


class CollectionExtractionRequestModel(RequestConfigModel):
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )


class ExtractionRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time: Timestamp | TimestampLatest | TimeRangeRequestModel = "latest"
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )

    # TODO: do we need these properties?
    @computed_field
    @property
    def timestamp_start(self) -> datetime | Literal["earliest", "latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return self.time.start
        return self.time

    @computed_field
    @property
    def timestamp_end(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return self.time.end
        return self.time


class ExtractionQueryParametersModel(
    AoiQueryModel,
    FilterRequestModel,
):
    time: Timestamp | TimestampLatest = "latest"
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )

    # TODO: do we need these properties?
    @computed_field
    @property
    def timestamp_start(self) -> datetime | Literal["earliest", "latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return self.time.start
        return self.time

    @computed_field
    @property
    def timestamp_end(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return self.time.end
        return self.time

    pass


class CollectionsExtractionRequestParametersModel(
    AoiRequestModel,
    CollectionsFilterRequestModel,
    CollectionsMemberFilterRequestModel,
    CollectionExtractionRequestModel,
):
    time: Timestamp | TimestampLatest = "latest"


class CollectionsExtractionQueryParametersModel(
    AoiQueryModel,
    CollectionsFilterRequestModel,
    CollectionsMemberFilterRequestModel,
    CollectionExtractionRequestModel,
):
    time: Timestamp | TimestampLatest = "latest"


class TimeBinsRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time_bins: TimeBinsRequestModel


class TimeSeriesRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
    GroupByRequestModel,
):
    time_series: TimeSeriesRequestModel
