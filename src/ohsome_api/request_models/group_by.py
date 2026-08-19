from typing import Literal

from pydantic import (
    BaseModel,
    Field,
)

from ohsome_api.request_models.config import RequestConfigModel


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
