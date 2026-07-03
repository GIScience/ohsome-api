from datetime import datetime
from importlib.metadata import version
from typing import cast

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import TimeBinColumns
from ohsome_api.request_models import Measure, TimeBinsParameters
from ohsome_api.response_models import (
    TimeBinsColumnsResponseModel,
    TimeBinsResponseModel,
)
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
    "/currentness/{measure}.json",
    response_class=JSONResponse,
    response_model=TimeBinsColumnsResponseModel,
    summary="Currentness of features in time bins.",
    tags=["History Statistics"],
)
async def post_currentness_as_json(
    parameters: TimeBinsParameters,
    measure: Measure,
) -> dict[str, TimeBinColumns]:
    return {
        "result": await service.get_currentness_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time_bins.start),
            end=parameters.time_bins.end,
            bin_size=parameters.time_bins.bin_size,
            aoi_wkt=parameters.aoi_wkt,
            measure=measure,
        )
    }


@router.post(
    "/currentness/{measure}.csv",
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
    summary="Currentness of features in time bins.",
    description=CSV_RESPONSE_DESCRIPTION,
    tags=["History Statistics"],
)
async def post_currentness_as_csv(
    parameters: TimeBinsParameters,
    measure: Measure,
) -> dict[str, list]:
    return {
        "result": await service.get_currentness_row(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time_bins.start),
            end=parameters.time_bins.end,
            bin_size=parameters.time_bins.bin_size,
            aoi_wkt=parameters.aoi_wkt,
            measure=measure,
        )
    }
