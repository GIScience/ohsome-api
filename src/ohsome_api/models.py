from datetime import datetime
from typing import TypedDict

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel


class Attribution(BaseModel):
    url: str = "https://ohsome.org/copyrights"
    text: str = "© OpenStreetMap contributors"


class Metadata(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    latest_timestamp: datetime
    earliest_timestamp: datetime


class TimeBinRow(BaseModel):
    value: int
    start: datetime
    end: datetime


class TimeBinColumns(BaseModel):
    start: list[datetime]
    end: list[datetime]
    value: list[int]


class SnapshotRow(BaseModel):
    value: int
    timestamp: datetime


class SnapshotColumns(BaseModel):
    timestamp: list[datetime]
    value: list[int]


class ExtractionRow(TypedDict):
    osm_type: str
    osm_id: int
    valid_from: datetime
    osm_version: int
    osm_minor_version: int
    osm_edits: int
    user_id: int
    user_name: str
    changeset_id: int
    tags: dict[str, str]
    xmin: float
    xmax: float
    ymin: float
    ymax: float
    geom: bytes
    clipped: bool
