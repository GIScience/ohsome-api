from importlib.metadata import version
from typing import Annotated

from fastapi import APIRouter, Depends, Query
from fastapi.responses import StreamingResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import (
    ExtractionQueryParametersModel,
    ExtractionRequestParametersModel,
)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


FEATURES_EXTRACT_DESCRIPTION = (
    "Nodes, ways, and relations tagged as `type=multipolygon` or `type=boundary` "
    "are included. "
    "Other relations can be queried with the `/extraction/collections.*` endpoints."
)


@router.post(
    "/extraction/features.parquet",
    response_class=StreamingResponse,
    summary="Download features.",
    description=FEATURES_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def post_features_extract(
    parameters: ExtractionRequestParametersModel,
) -> StreamingResponse:
    return await features_extract(parameters)


@router.get(
    "/extraction/features.parquet",
    response_class=StreamingResponse,
    summary="Download features.",
    description=FEATURES_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def get_features_extract(
    parameters: Annotated[
        ExtractionQueryParametersModel,
        Query(),
    ],
) -> StreamingResponse:
    return await features_extract(parameters)


async def features_extract(
    parameters: ExtractionRequestParametersModel | ExtractionQueryParametersModel,
) -> StreamingResponse:
    stream = await service.extract_features_as_parquet(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.start,
        parameters.end,
        False,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.parquet",
        headers={"Content-Disposition": 'attachment; filename="features.parquet"'},
    )


@router.post(
    "/extraction/features.arrow",
    response_class=StreamingResponse,
    summary="Download features.",
    description=FEATURES_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
    include_in_schema=False,
)
async def post_features_extract_arrow(
    parameters: ExtractionRequestParametersModel,
) -> StreamingResponse:
    return await features_extract_as_arrow(parameters)


@router.get(
    "/extraction/features.arrow",
    response_class=StreamingResponse,
    summary="Download features.",
    description=FEATURES_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
    include_in_schema=False,
)
async def get_features_extract_arrow(
    parameters: Annotated[
        ExtractionQueryParametersModel,
        Query(),
    ],
) -> StreamingResponse:
    return await features_extract_as_arrow(parameters)


async def features_extract_as_arrow(
    parameters: ExtractionRequestParametersModel | ExtractionQueryParametersModel,
) -> StreamingResponse:

    stream = await service.extract_features_as_arrow(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.start,
        parameters.end,
        False,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.arrow",
        headers={"Content-Disposition": 'attachment; filename="features.arrow"'},
    )


FEATURES_COLLECTIONS_EXTRACT_DESCRIPTION = (
    "Returns relations (not tagged as `type=multipolygon` or `type=boundary`) "
    "as geometry collections. "
    "For each relation a separate row is returned for their linear, polygonal "
    "or point members"
)

FEATURES_COLLECTIONS_MEMBERS_EXTRACT_DESCRIPTION = (
    "Returns relations (not tagged as `type=multipolygon` or `type=boundary`) members."
    "For each relation all members features are returned row by row."
)

FEATURES_COLLECTIONS_EXTRACT_EXAMPLE = {
    "example": {
        "filter": "type:relation and type=route and route=bus and service=night",
        "timestamp": "latest",
        "member_filter": "geometry:line",
        "aoi": [8.68812, 49.4039, 8.72362, 49.41582],
        "clip": True,
    }
}
