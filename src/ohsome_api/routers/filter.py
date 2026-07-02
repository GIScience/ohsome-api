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
async def validate_filter(
    query_params: Annotated[FilterRequestModel, Query()],
) -> dict:
    # TODO add ohsome_filter_to_sql error handling
    return {"filter": query_params.ohsome_filter}
