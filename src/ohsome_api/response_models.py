"""Shared response models."""

from importlib.metadata import version

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

from ohsome_api.models import (
    Attribution,
    SnapshotColumns,
    SnapshotRow,
    TimeBinColumns,
    TimeBinRow,
)

VERSION = version("ohsome-api")


class BaseResponseModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    api_version: str = VERSION
    attribution: Attribution = Attribution()


class TimeBinsResponseModel(BaseResponseModel):
    result: list[TimeBinRow]


class TimeBinsColumnsResponseModel(BaseResponseModel):
    result: TimeBinColumns


class SnapshotsResponseModel(BaseResponseModel):
    result: list[SnapshotRow]


class SnapshotColumnsResponseModel(BaseResponseModel):
    result: SnapshotColumns
