from fastapi import APIRouter

from ohsome_api import service
from ohsome_api.models import Metadata
from ohsome_api.response_models import BaseResponseModel

router = APIRouter()


class MetadataResponseModel(BaseResponseModel):
    temporal_extent: Metadata


@router.get(
    "/metadata",
    summary="Metadata of the underlying database (ohsomedb).",
    tags=["Metadata"],
    response_model=MetadataResponseModel,
)
async def get_metadata() -> dict[str, Metadata]:
    metadata = await service.get_ohsomedb_metadata()
    return {"temporal_extent": metadata}
