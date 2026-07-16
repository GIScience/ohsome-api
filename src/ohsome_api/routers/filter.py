from typing import Annotated

from fastapi import APIRouter, Query

from ohsome_api.request_models import FilterRequestModel
from ohsome_api.response_models import FilterResponseModel

router = APIRouter()


@router.get(
    "/filter/validation",
    summary="Validate filter syntax.",
    tags=["Filter Validation"],
    response_model=FilterResponseModel,
)
async def validate_filter_get(
    parameters: Annotated[FilterRequestModel, Query()],
) -> dict:
    return {"filter": parameters.ohsome_filter}


@router.post(
    "/filter/validation",
    summary="Validate filter syntax.",
    tags=["Filter Validation"],
    response_model=FilterResponseModel,
)
async def validate_filter_post(
    parameters: FilterRequestModel,
) -> dict:
    return {"filter": parameters.ohsome_filter}
