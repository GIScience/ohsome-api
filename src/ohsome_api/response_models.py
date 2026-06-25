"""Shared response models."""

from importlib.metadata import version

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

from ohsome_api.models import SnapshotRowModel, TimeBinRowModel

VERSION = version("ohsome-api")


class Attribution(BaseModel):
    url: str = "https://ohsome.org/copyrights"
    text: str = "© OpenStreetMap contributors"


class BaseResponseModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    api_version: str = VERSION
    attribution: Attribution = Attribution()


class TimeBinsResponseModel(BaseResponseModel):
    result: list[TimeBinRowModel]


class SnapshotsResponseModel(BaseResponseModel):
    result: list[SnapshotRowModel]
