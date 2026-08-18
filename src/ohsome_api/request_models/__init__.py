from datetime import datetime, timedelta
from enum import StrEnum
from typing import Literal, cast

from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    AliasChoices,
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
    Timestamp,
    TimestampEarliest,
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
        title="Filter",
        description=(
            "Filter for OSM data. "
            "Please refer to the [ohsome filter language documentation]"
            f"({CONFIG.external_docs_url}/reference.html#filter)."
        ),
        json_schema_extra={"example": "type:node and natural=tree"},
    )


class GroupByTagModel(BaseModel):
    type: Literal["byTag"]
    key: str


class GroupByRequestModel(RequestConfigModel):
    group_by: GroupByTagModel | None = Field(
        default=None,
        description=(
            "`(experimental, optional)`; If given indicates that the "
            "results should also values for individual subsets of the "
            "result defined by the presence of tags with the given key"
        ),
        json_schema_extra={
            "examples": [None],
        },
    )


class ExtractionQueryModel(RequestConfigModel):
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )
    timestamp: Timestamp | TimestampLatest | TimestampEarliest = Field(
        json_schema_extra={"examples": ["latest", "2026-04-17T00:00:00Z"]},
    )

    @computed_field
    @property
    def timestamp_start(self) -> datetime | Literal["latest"]:
        return cast(datetime | Literal["latest"], self.timestamp)

    @computed_field
    @property
    def timestamp_end(self) -> datetime | Literal["latest"]:
        return cast(datetime | Literal["latest"], self.timestamp)


class ExtractionRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time: Timestamp | TimestampLatest | TimeRangeRequestModel
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
    time: Timestamp | TimestampLatest
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


class TimeBinsRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time: TimeBinsRequestModel = Field(
        validation_alias=AliasChoices("time", "timeBins")
    )
    clip: bool = Field(
        default=False,
        description=(
            "If true, length and area calculations use the clipped feature geometries. "
            "Clipping can be computationally expensive for large AOIs, "
            "depending on your ohsome filter, and is usually unnecessary."
        ),
    )
