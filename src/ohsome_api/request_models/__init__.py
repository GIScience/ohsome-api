from datetime import datetime, timedelta
from enum import StrEnum
from typing import Literal, Optional, cast

from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    computed_field,
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


class ExtractionRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time: Timestamp | TimestampLatest | TimeRangeRequestModel = "latest"
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )

    @computed_field
    @property
    def start(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return cast(datetime | Literal["latest"], self.time.start)
        return self.time

    @computed_field
    @property
    def end(self) -> datetime | Literal["latest"]:
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

    @computed_field
    @property
    def start(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return cast(datetime | Literal["latest"], self.time.start)
        return self.time

    @computed_field
    @property
    def end(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return self.time.end
        return self.time

    pass


class CollectionsExtractionRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    # TODO: Change example of ohsome filter
    time: Timestamp | TimestampLatest = "latest"
    member_filter: OhsomeFilter = Field(
        default="*",
        description="Specific ohsome filter for members.",
        json_schema_extra={"example": "geometry:line"},
    )
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )


class CollectionsExtractionQueryParametersModel(
    AoiQueryModel,
    FilterRequestModel,
):
    # TODO: Change example of ohsome filter
    time: Timestamp | TimestampLatest = "latest"
    member_filter: OhsomeFilter = Field(
        default="*",
        description="Specific ohsome filter for members.",
        json_schema_extra={"example": "geometry:line"},
    )
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )


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
