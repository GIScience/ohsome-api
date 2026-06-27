from datetime import datetime
from importlib.metadata import version
from typing import cast

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import Measure, TimeBinsParameters
from ohsome_api.response_models import TimeBinsResponseModel
from ohsome_api.response_renderers import (
    CURRENTNESS_AS_CSV_EXAMPLE,
    CSVTimeBinsResponse,
)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


@router.post(
    "/currentness/{measure}.json",
    response_class=JSONResponse,
    summary="Currentness of features",
    tags=["History Statistics"],
)
async def post_currentness_as_json(
    parameters: TimeBinsParameters,
    measure: Measure,
) -> TimeBinsResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=cast(datetime, parameters.time_bins.start),
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi_wkt,
        measure=measure,
    )
    return TimeBinsResponseModel(result=result)


@router.post(
    "/currentness/{measure}.csv",
    response_class=CSVTimeBinsResponse,
    responses={
        200: CURRENTNESS_AS_CSV_EXAMPLE,
    },
    summary="Currentness of features",
    tags=["History Statistics"],
)
async def post_currentness_as_csv(
    parameters: TimeBinsParameters,
    measure: Measure,
) -> TimeBinsResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=cast(datetime, parameters.time_bins.start),
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi_wkt,
        measure=measure,
    )
    return TimeBinsResponseModel(result=result)
