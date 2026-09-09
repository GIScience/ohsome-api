from importlib.metadata import version

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import Field

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import Measure, TimeBinsResult
from ohsome_api.request_models import FilterRequestModel
from ohsome_api.request_models.aoi import AoiRequestModel
from ohsome_api.request_models.time import TimeBins
from ohsome_api.response_models import BaseResponseModel
from ohsome_api.response_renderers import CSVTimeBinsResponse

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


class StatsCurrentnessRequest(
    AoiRequestModel,
    FilterRequestModel,
):
    time: TimeBins
    clip: bool = Field(
        default=False,
        description=(
            "If true, length and area calculations use the clipped feature geometries. "
            "Clipping can be computationally expensive for large AOIs, "
            "depending on your ohsome filter, and is usually unnecessary."
        ),
    )


class StatsCurrentnessResponse(BaseResponseModel):
    result: TimeBinsResult


@router.post(
    "/stats/currentness/{measure}.json",
    response_class=JSONResponse,
    response_model=StatsCurrentnessResponse,
    summary="Currentness of features in time bins.",
    tags=["Statistics"],
)
async def post_currentness_as_json(
    parameters: StatsCurrentnessRequest,
    measure: Measure,
) -> dict[str, TimeBinsResult]:
    return {
        "result": await service.get_currentness_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=parameters.time.start,
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
            measure=measure,
            clip=parameters.clip,
        )
    }


@router.post(
    "/stats/currentness/{measure}.csv",
    response_class=CSVTimeBinsResponse,
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": CSVTimeBinsResponse.example,
                },
            },
        },
    },
    summary="Currentness of features in time bins.",
    description=CSVTimeBinsResponse.description,
    tags=["Statistics"],
)
async def post_currentness_as_csv(
    parameters: StatsCurrentnessRequest,
    measure: Measure,
) -> dict[str, list]:
    return {
        "result": await service.get_currentness_row(
            ohsome_filter=parameters.ohsome_filter,
            start=parameters.time.start,
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
            measure=measure,
            clip=parameters.clip,
        )
    }
