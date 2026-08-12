from importlib.metadata import version
from typing import Annotated

from fastapi import APIRouter, Depends, Query
from fastapi.responses import StreamingResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import (
    CollectionsExtractionQueryParametersModel,
    CollectionsExtractionRequestParametersModel,
    ExtractionQueryParametersModel,
    ExtractionRequestParametersModel,
)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


CONTRIBUTIONS_EXTRACT_DESCRIPTION = (
    "Nodes, ways, and relations tagged as `type=multipolygon` or `type=boundary` "
    "are included. "
    "Other relations can be queried with the `/extraction/collections.*` endpoints."
)


@router.post(
    "/extraction/features.parquet",
    response_class=StreamingResponse,
    summary="Download features.",
    description=CONTRIBUTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def post_contributions_extract(
    parameters: ExtractionRequestParametersModel,
) -> StreamingResponse:
    return await contributions_extract(parameters)


@router.get(
    "/extraction/features.parquet",
    response_class=StreamingResponse,
    summary="Download features.",
    description=CONTRIBUTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def get_contributions_extract(
    parameters: Annotated[
        ExtractionQueryParametersModel,
        Query(),
    ],
) -> StreamingResponse:
    return await contributions_extract(parameters)


async def contributions_extract(
    parameters: ExtractionRequestParametersModel | ExtractionQueryParametersModel,
) -> StreamingResponse:
    stream = await service.extract_features_as_parquet(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.timestamp_start,
        parameters.timestamp_end,
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
    description=CONTRIBUTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def post_contributions_extract_arrow(
    parameters: ExtractionRequestParametersModel,
) -> StreamingResponse:
    return await contributions_extract_as_arrow(parameters)


@router.get(
    "/extraction/features.arrow",
    response_class=StreamingResponse,
    summary="Download features.",
    description=CONTRIBUTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def get_contributions_extract_arrow(
    parameters: Annotated[
        ExtractionQueryParametersModel,
        Query(),
    ],
) -> StreamingResponse:
    return await contributions_extract_as_arrow(parameters)


async def contributions_extract_as_arrow(
    parameters: ExtractionRequestParametersModel | ExtractionQueryParametersModel,
) -> StreamingResponse:

    stream = await service.extract_features_as_arrow(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.timestamp_start,
        parameters.timestamp_end,
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


@router.post(
    "/extraction/collections.parquet",
    response_class=StreamingResponse,
    summary="Download collections.",
    description=FEATURES_COLLECTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def post_features_collections_extract(
    parameters: CollectionsExtractionRequestParametersModel,
) -> StreamingResponse:
    return await features_collections_extract(parameters)


@router.get(
    "/extraction/collections.parquet",
    response_class=StreamingResponse,
    summary="Download collections.",
    description=FEATURES_COLLECTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def get_features_collections_extract(
    parameters: Annotated[
        CollectionsExtractionQueryParametersModel,
        Query(),
    ],
) -> StreamingResponse:
    return await features_collections_extract(parameters)


async def features_collections_extract(
    parameters: CollectionsExtractionRequestParametersModel
    | CollectionsExtractionQueryParametersModel,
) -> StreamingResponse:
    stream = await service.extract_features_collections_as_parquet(
        parameters.ohsome_filter,
        parameters.member_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.timestamp,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.parquet",
        headers={"Content-Disposition": 'attachment; filename="collections.parquet"'},
    )


@router.post(
    "/extraction/collections_members.parquet",
    response_class=StreamingResponse,
    summary="Download collections members.",
    description=FEATURES_COLLECTIONS_MEMBERS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def post_features_collections_members_extract(
    parameters: CollectionsExtractionRequestParametersModel,
) -> StreamingResponse:
    return await features_collections_members_extract(parameters)


@router.get(
    "/extraction/collections_members.parquet",
    response_class=StreamingResponse,
    summary="Download collections members.",
    description=FEATURES_COLLECTIONS_MEMBERS_EXTRACT_DESCRIPTION,
    tags=["Extraction"],
)
async def get_features_collections_members_extract(
    parameters: Annotated[
        CollectionsExtractionQueryParametersModel,
        Query(),
    ],
) -> StreamingResponse:
    return await features_collections_members_extract(parameters)


async def features_collections_members_extract(
    parameters: CollectionsExtractionRequestParametersModel
    | CollectionsExtractionQueryParametersModel,
) -> StreamingResponse:
    stream = await service.extract_features_collections_members_as_parquet(
        parameters.ohsome_filter,
        parameters.member_filter,
        parameters.aoi_wkt,
        parameters.clip,
        parameters.timestamp,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.parquet",
        headers={
            "Content-Disposition": 'attachment; filename="collections_members.parquet"'
        },
    )
