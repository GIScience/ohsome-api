from importlib.metadata import version

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse, StreamingResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import BaseParameters, Measure, TimeSeriesParameters
from ohsome_api.response_models import SnapshotsResponseModel
from ohsome_api.response_renderers import (
    FEATURES_AS_CSV_EXAMPLE,
    CSVSnapshotsResponse,
)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


@router.post(
    "/features/{measure}.json",
    response_class=JSONResponse,
    summary="Aggregate features by {measure}",
    tags=["History Statistics"],
)
async def post_features_as_json(
    parameters: TimeSeriesParameters,
    measure: Measure,
) -> SnapshotsResponseModel:
    result = await service.get_features(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_series.start,
        end=parameters.time_series.end,
        interval=parameters.time_series.interval,
        aoi_wkt=parameters.aoi_wkt,
        measure=measure,
    )
    return SnapshotsResponseModel(result=result)


@router.post(
    "/features/{measure}.csv",
    response_class=CSVSnapshotsResponse,
    responses={
        200: FEATURES_AS_CSV_EXAMPLE,
    },
    summary="Aggregate features by {measure}",
    tags=["History Statistics"],
)
async def post_features_as_csv(
    parameters: TimeSeriesParameters,
    measure: Measure,
) -> SnapshotsResponseModel:
    result = await service.get_features(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_series.start,
        end=parameters.time_series.end,
        interval=parameters.time_series.interval,
        aoi_wkt=parameters.aoi_wkt,
        measure=measure,
    )
    return SnapshotsResponseModel(result=result)


@router.post(
    "/features/extraction.parquet",
    response_class=StreamingResponse,
    summary="Download features",
    tags=["Data Extraction"],
)
async def post_contributions_extract(
    parameters: BaseParameters,
) -> StreamingResponse:
    parquet_stream = await service.extract_features_as_parquet(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
    )
    return StreamingResponse(
        parquet_stream,
        media_type="application/vnd.apache.parquet",
        headers={"Content-Disposition": 'attachment; filename="extractions.parquet"'},
    )
