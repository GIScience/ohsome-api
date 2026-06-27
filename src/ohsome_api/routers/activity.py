from datetime import datetime
from importlib.metadata import version
from typing import cast

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import TimeBinRow
from ohsome_api.request_models import TimeBinsParameters
from ohsome_api.response_models import TimeBinsResponseModel
from ohsome_api.response_renderers import (
    CSV_RESPONSE_DESCRIPTION,
    CSV_TIME_BINS_RESPONSE_EXAMPLE,
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
    summary="Active users per time bin.",
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
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": CSV_TIME_BINS_RESPONSE_EXAMPLE,
                },
            },
        },
    },
    summary="Active users per time bin.",
    description=CSV_RESPONSE_DESCRIPTION,
    tags=["History Statistics"],
)
async def post_users_activity_as_csv(
    parameters: TimeBinsParameters,
) -> dict[str, list[TimeBinRow]]:
    return await users_activity(parameters)


async def users_activity(
    parameters: TimeBinsParameters,
) -> dict[str, list[TimeBinRow]]:
    return {
        "result": await service.get_users_activity(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time_bins.start),
            end=parameters.time_bins.end,
            bin_size=parameters.time_bins.bin_size,
            aoi_wkt=parameters.aoi_wkt,
        )
    }
