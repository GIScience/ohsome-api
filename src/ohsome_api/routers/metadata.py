from fastapi import APIRouter

from ohsome_api import service
from ohsome_api.models import Metadata
from ohsome_api.response_models import BaseResponseModel

router = APIRouter()


class MetadataResponse(BaseResponseModel):
    temporal_extent: Metadata


@router.get(
    "/metadata",
    summary="Metadata of the underlying database.",
    tags=["Metadata"],
    response_model=MetadataResponse,
)
async def get_metadata() -> dict[str, Metadata]:
    metadata = await service.get_ohsomedb_metadata()
    return {"temporal_extent": metadata}
