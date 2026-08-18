from importlib.metadata import version
from typing import Annotated

from fastapi import APIRouter, Depends, Query
from fastapi.responses import StreamingResponse
from ohsome_filter_to_sql import OhsomeFilter
from pydantic import Field, computed_field

from ohsome_api import service
from ohsome_api.config import CONFIG
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import (
    RequestConfigModel,
)
from ohsome_api.request_models.aoi import AoiQueryModel, AoiRequestModel
from ohsome_api.request_models.time import Timestamp, TimestampLatest

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
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


class CollectionsFilter(RequestConfigModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        title="Filter",
        description=(
            "Filter for OSM data. "
            "Please refer to the [ohsome filter language documentation]"
            f"({CONFIG.external_docs_url}/reference.html#filter)."
        ),
        json_schema_extra={
            "example": "type:relation and type=route and route=bus and service=night"
        },
    )


class CollectionsExtractionRequestParametersModel(
    AoiRequestModel,
    CollectionsFilter,
):
    time: Timestamp | TimestampLatest
    member_filter_: OhsomeFilter | None = Field(
        default=None,
        alias="member_filter",
        description="Specific ohsome filter for members.",
        json_schema_extra={"example": "geometry:line"},
    )
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )

    @computed_field
    @property
    def member_filter(self) -> OhsomeFilter:
        if self.member_filter_ is None:
            return "*"

        return self.member_filter_


class CollectionsExtractionQueryParametersModel(
    AoiQueryModel,
    CollectionsFilter,
):
    time: Timestamp | TimestampLatest
    member_filter_: OhsomeFilter | None = Field(
        default=None,
        alias="member_filter",
        description="Specific ohsome filter for members.",
        json_schema_extra={"example": "geometry:line"},
    )
    clip: bool = Field(
        default=True,
        description="Whether to clip extracted features with AOI or not.",
    )

    @computed_field
    @property
    def member_filter(self) -> OhsomeFilter:
        if self.member_filter_ is None:
            return "*"

        return self.member_filter_


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
        parameters.time,
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
        parameters.time,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.parquet",
        headers={
            "Content-Disposition": 'attachment; filename="collections_members.parquet"'
        },
    )
