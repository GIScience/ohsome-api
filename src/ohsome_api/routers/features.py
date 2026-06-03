from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import FeaturesRowModel
from ohsome_api.request_models import Measure, TimeSeriesParameters
from ohsome_api.response_models import BaseResponseModel

router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


class FeaturesResponseModel(BaseResponseModel):
    result: list[FeaturesRowModel]


@router.post("/features/{measure}.json", response_class=JSONResponse)
async def post_features_as_json(
    parameters: TimeSeriesParameters,
    measure: Measure,
) -> FeaturesResponseModel:
    result = await service.get_features(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_series.start,
        end=parameters.time_series.end,
        interval=parameters.time_series.interval,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return FeaturesResponseModel(result=result)
