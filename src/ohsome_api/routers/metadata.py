import asyncio

from fastapi import APIRouter

from ohsome_api import service
from ohsome_api.models import Metadata
from ohsome_api.response_models import BaseResponseModel

router = APIRouter()


class MetadataResponseModel(BaseResponseModel):
    temporal_extent: Metadata


@router.get(
    "/metadata",
    summary="Metadata of the underlying database.",
    tags=["Metadata"],
    response_model=MetadataResponseModel,
)
async def get_metadata() -> dict[str, Metadata]:
    metadata = await service.get_ohsomedb_metadata()
    return {"temporal_extent": metadata}


@router.get(
    "/debug_timeout",
    summary="Waits 180 seconds and returns metadata",
    tags=["Debug"],
    response_model=MetadataResponseModel,
    include_in_schema=False,
)
async def debug_timeout() -> dict[str, Metadata]:
    await asyncio.sleep(180)
    metadata = await service.get_ohsomedb_metadata()
    return {"temporal_extent": metadata}
