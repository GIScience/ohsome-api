from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import TimeBinsParameters
from ohsome_api.response_models import CountResponseModel

router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


@router.post("/activity/users.json", response_class=JSONResponse)
async def post_users_activity_as_json(
    parameters: TimeBinsParameters,
) -> CountResponseModel:
    result = await service.get_users_activity(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
    )

    return CountResponseModel(result=result)
