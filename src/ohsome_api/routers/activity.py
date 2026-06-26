from importlib.metadata import version

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import TimeBinRow
from ohsome_api.request_models import TimeBinsParameters
from ohsome_api.response_models import TimeBinsResponseModel
from ohsome_api.response_renderers import (
    POST_ACTIVITY_AS_CSV_EXAMPLE,
    CSVTimeBinsResponse,
)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


@router.post(
    "/activity/users.json",
    response_class=JSONResponse,
    response_model=TimeBinsResponseModel,
    summary="Active users per time bin",
    tags=["History Statistics"],
)
async def post_users_activity_as_json(
    parameters: TimeBinsParameters,
) -> dict[str, list[TimeBinRow]]:
    return await users_activity(parameters)


@router.post(
    "/activity/users.csv",
    response_class=CSVTimeBinsResponse,
    response_model=TimeBinsResponseModel,
    responses={200: POST_ACTIVITY_AS_CSV_EXAMPLE},
)
async def post_users_activity_as_csv(
    parameters: TimeBinsParameters,
) -> dict[str, list[TimeBinRow]]:
    """Active users per time bin."""
    return await users_activity(parameters)


async def users_activity(
    parameters: TimeBinsParameters,
) -> dict[str, list[TimeBinRow]]:
    return {
        "result": await service.get_users_activity(
            ohsome_filter=parameters.ohsome_filter,
            start=parameters.time_bins.start,
            end=parameters.time_bins.end,
            bin_size=parameters.time_bins.bin_size,
            aoi_wkt=parameters.aoi_wkt,
        )
    }
