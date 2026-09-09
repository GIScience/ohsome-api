from typing import Annotated

from fastapi import APIRouter, Query, Response

from ohsome_api.request_models import FilterRequestModel

router = APIRouter()


class FilterRequest(FilterRequestModel):
    pass


@router.post(
    "/filter/validation",
    response_class=Response,
    summary="Validate filter syntax.",
    tags=["Filter Validation"],
)
async def validate_filter_post(
    _parameters: FilterRequest,
) -> None:
    pass


@router.get(
    "/filter/validation",
    response_class=Response,
    summary="Validate filter syntax.",
    tags=["Filter Validation"],
)
async def validate_filter_get(
    _parameters: Annotated[FilterRequest, Query()],
) -> None:
    pass
