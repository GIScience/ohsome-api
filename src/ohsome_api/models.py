from datetime import datetime
from typing import Protocol, TypedDict

from pydantic import BaseModel


# TODO: find common name scheme for row and response models
class TimeBinsRowModel(BaseModel):
    value: int
    start: datetime
    end: datetime


class FeaturesRowModel(BaseModel):
    value: int
    timestamp: datetime


# TODO: Use Pydantic Model to make sure openapi schema is generated?
class ExtractionRow(TypedDict):
    osm_type: str
    osm_id: int
    valid_from: datetime
    osm_version: int
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


class ExtractionWriter(Protocol):
    def write_batch(self, batch: list[ExtractionRow]) -> None: ...
    def close(self) -> None: ...
