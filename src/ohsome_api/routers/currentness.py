from importlib.metadata import version

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import Measure, TimeBinsParameters
from ohsome_api.response_models import CountResponseModel
from ohsome_api.response_renderers import CSVCountResponse

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
) -> CountResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return CountResponseModel(result=result)


@router.post(
    "/currentness/{measure}.csv",
    response_class=CSVCountResponse,
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start;end;value
1970-01-01T00:00:00Z;1970-02-01T00:00:00Z;0
""",
                },
            },
        },
    },
    summary="Currentness of features",
    tags=["History Statistics"],
)
async def post_currentness_as_csv(
    parameters: TimeBinsParameters,
    measure: Measure,
) -> CountResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return CountResponseModel(result=result)
