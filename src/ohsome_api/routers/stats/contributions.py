from datetime import datetime
from importlib.metadata import version
from typing import cast

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import TimeBinColumns, TimeBinRow
from ohsome_api.request_models import FilterRequestModel
from ohsome_api.request_models.aoi import AoiRequestModel
from ohsome_api.request_models.time import TimeBinsRequestModel
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


class TimeBinsRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time: TimeBinsRequestModel


@router.post(
    "/stats/contributions/count.json",
    response_class=JSONResponse,
    response_model=TimeBinsColumnsResponseModel,
    summary="Contributions per time bin.",
    tags=["Statistics (Experimental)"],
)
async def post_contributors_count_as_json(
    parameters: TimeBinsRequestParametersModel,
) -> dict[str, TimeBinColumns]:
    return {
        "result": await service.get_contributions_count_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time.start),  # ty: ignore[redundant-cast]
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
        )
    }


@router.post(
    "/stats/contributions/count.csv",
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
    summary="Active contributors per time bin.",
    description=CSV_RESPONSE_DESCRIPTION,
    tags=["Statistics (Experimental)"],
)
async def post_contributors_count_as_csv(
    parameters: TimeBinsRequestParametersModel,
) -> dict[str, list[TimeBinRow]]:
    return {
        "result": await service.get_contributions_count_rows(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time.start),  # ty: ignore[redundant-cast]
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
        )
    }
