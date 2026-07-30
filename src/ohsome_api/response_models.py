"""Shared response models."""

from importlib.metadata import version

from ohsome_filter_to_sql import OhsomeFilter
from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

from ohsome_api.models import (
    Attribution,
    SnapshotColumnsGrouped,
    SnapshotRow,
    TimeBinColumns,
    TimeBinRow,
)

VERSION = version("ohsome-api")


class BaseResponseModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    api_version: str = VERSION
    attribution: Attribution = Attribution()


class FilterResponseModel(BaseResponseModel):
    filter: OhsomeFilter


class TimeBinsResponseModel(BaseResponseModel):
    result: list[TimeBinRow]


class TimeBinsColumnsResponseModel(BaseResponseModel):
    result: TimeBinColumns


class SnapshotsResponseModel(BaseResponseModel):
    result: list[SnapshotRow]


class SnapshotColumnsResponseModel(BaseResponseModel):
    result: SnapshotColumnsGrouped
