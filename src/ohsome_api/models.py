from datetime import datetime
from typing import Literal, TypedDict

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel


class Attribution(BaseModel):
    url: str = "https://ohsome.org/copyrights"
    text: str = "© OpenStreetMap contributors"


class Metadata(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    start: datetime
    end: datetime


Measure = Literal["count", "length", "area"]


class TimeBinsRowResult(BaseModel):
    value: int
    start: datetime
    end: datetime


class TimeBinsResult(BaseModel):
    start: list[datetime]
    end: list[datetime]
    value: list[int]


class TimeSeriesRowResult(BaseModel):
    value: int
    timestamp: datetime


class TimeSeriesRowGroupedByResult(TimeSeriesRowResult):
    tagvalue: str


class TimeSeriesResult(BaseModel):
    timestamp: list[datetime]
    value: list[int]


class TimeSeriesGroupedByResult(TimeSeriesResult):
    values: dict[str, list[int]]


class ExtractionRow(TypedDict):
    osm_type: str
    osm_id: int
    valid_from: datetime
    valid_to: datetime
    osm_version: int
    osm_minor_version: int
    osm_edits: int
    user_id: int
    user_name: str
    changeset_id: int
    changeset_tags: dict[str, str]
    contrib_type: str
    tags: dict[str, str]
    tags_before: dict[str, str]
    xmin: float
    xmax: float
    ymin: float
    ymax: float
    geom_type: str
    geom: bytes
    clipped: bool
    part_of: int
    part_of_role: str
    part_of_pos: int
