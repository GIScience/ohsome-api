from typing import Literal

from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
)
from pydantic.alias_generators import to_camel


class RequestConfigModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
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
