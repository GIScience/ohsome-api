from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import TimeBinsResult
from ohsome_api.request_models import FilterRequestModel
from ohsome_api.request_models.aoi import AoiRequestModel
from ohsome_api.request_models.time import TimeBins
from ohsome_api.response_models import BaseResponseModel
from ohsome_api.response_renderers import CSVTimeBinsResponse

router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


class StatsContributionsRequest(
    AoiRequestModel,
    FilterRequestModel,
):
    time: TimeBins


class StatsContributionsResponse(BaseResponseModel):
    result: TimeBinsResult


@router.post(
    "/stats/contributions/count.json",
    response_class=JSONResponse,
    response_model=StatsContributionsResponse,
    summary="Contributions per time bin.",
    tags=["Statistics (Experimental)"],
)
async def post_contributors_count_as_json(
    parameters: StatsContributionsRequest,
) -> dict[str, TimeBinsResult]:
    return {
        "result": await service.get_contributions_count_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=parameters.time.start,
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
        )
    }


@router.post(
    "/stats/contributions/count.csv",
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
    summary="Active contributors per time bin.",
    description=CSVTimeBinsResponse.description,
    tags=["Statistics (Experimental)"],
)
async def post_contributors_count_as_csv(
    parameters: StatsContributionsRequest,
) -> dict[str, list]:
    return {
        "result": await service.get_contributions_count_rows(
            ohsome_filter=parameters.ohsome_filter,
            start=parameters.time.start,
            end=parameters.time.end,
            bin_size=parameters.time.bin_size,
            aoi_wkt=parameters.aoi_wkt,
        )
    }
