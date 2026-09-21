from typing import Literal

from pydantic import (
    BaseModel,
    Field,
)

from ohsome_api.request_models.config import RequestConfigModel


class GroupByTag(BaseModel):
    type: Literal["byTag"]
    key: str


class GroupByRequestModel(RequestConfigModel):
    group_by: GroupByTag | None = Field(
        default=None,
        description=(
            "Splits the result into subsets for each distinct "
            + "value of the specified OSM tag key."
        ),
        json_schema_extra={
            "examples": [{"groupBy": {"type": "byTag", "key": "natural"}}]
        },
    )
