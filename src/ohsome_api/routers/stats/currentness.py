from datetime import datetime
from importlib.metadata import version
from typing import cast

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import AliasChoices, Field

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import MeasureEnum, TimeBinColumns
from ohsome_api.request_models import (
    FilterRequestModel,
    MeasureRequestModel,
)
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


# TODO: Rename request model to reflect currentness
class TimeBinsRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time: TimeBinsRequestModel = Field(
        validation_alias=AliasChoices("time", "timeBins")
    )
    clip: bool = Field(
        default=False,
        description=(
            "If true, length and area calculations use the clipped feature geometries. "
            "Clipping can be computationally expensive for large AOIs, "
            "depending on your ohsome filter, and is usually unnecessary."
        ),
    )


@router.post(
    "/stats/currentness/{measure}.json",
    response_class=JSONResponse,
    response_model=TimeBinsColumnsResponseModel,
    summary="Currentness of features in time bins.",
    tags=["Statistics"],
)
async def post_currentness_as_json(
    parameters: TimeBinsRequestParametersModel,
    measure: MeasureRequestModel,
) -> dict[str, TimeBinColumns]:
    return {
        "result": await service.get_currentness_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time.start),
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
            measure=cast(MeasureEnum, measure),
            clip=parameters.clip,
        )
    }


@router.post(
    "/stats/currentness/{measure}.csv",
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
    tags=["Statistics"],
)
async def post_currentness_as_csv(
    parameters: TimeBinsRequestParametersModel,
    measure: MeasureRequestModel,
) -> dict[str, list]:
    return {
        "result": await service.get_currentness_row(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time.start),
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
            measure=cast(MeasureEnum, measure),
            clip=parameters.clip,
        )
    }
