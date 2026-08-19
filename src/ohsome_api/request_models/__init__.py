from pydantic import (
    BaseModel,
    ConfigDict,
)
from pydantic.alias_generators import to_camel

from ohsome_api.request_models.filter import FilterRequestModel
from ohsome_api.request_models.group_by import GroupByRequestModel, GroupByTagModel
from ohsome_api.request_models.measure import MeasureRequestModel

__all__ = (
    "FilterRequestModel",
    "GroupByRequestModel",
    "GroupByTagModel",
    "MeasureRequestModel",
)


class RequestConfigModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
    )
