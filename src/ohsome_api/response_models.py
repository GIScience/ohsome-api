"""Shared response models."""

from importlib.metadata import version

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

from ohsome_api.models import TimeBinsRowModel

VERSION = version("ohsome-api")


class Attribution(BaseModel):
    url: str = "https://ohsome.org/copyrights"
    text: str = "© OpenStreetMap contributors"


class BaseResponseModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    api_version: str = VERSION
    attribution: Attribution = Attribution()


# TODO: Rename
class CountResponseModel(BaseResponseModel):
    result: list[TimeBinsRowModel]
