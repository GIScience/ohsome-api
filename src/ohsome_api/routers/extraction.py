from importlib.metadata import version

from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import (
    ExtractionRequestParametersModel,
)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


@router.post(
    "/extraction/features.parquet",
    response_class=StreamingResponse,
    summary="Download features.",
    tags=["Extraction"],
)
async def post_contributions_extract(
    parameters: ExtractionRequestParametersModel,
) -> StreamingResponse:
    stream = await service.extract_features_as_parquet(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.timestamp,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.parquet",
        headers={"Content-Disposition": 'attachment; filename="extractions.parquet"'},
    )


@router.post(
    "/extraction/features.arrow",
    response_class=StreamingResponse,
    summary="Download features.",
    tags=["Extraction"],
)
async def post_contributions_extract_arrow(
    parameters: ExtractionRequestParametersModel,
) -> StreamingResponse:
    stream = await service.extract_features_as_arrow(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.timestamp,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.arrow",
        headers={"Content-Disposition": 'attachment; filename="extractions.arrow"'},
    )
