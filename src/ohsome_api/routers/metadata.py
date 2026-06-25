from fastapi import APIRouter

from ohsome_api import service
from ohsome_api.response_models import BaseResponseModel

router = APIRouter()


class MetadataResponseModel(BaseResponseModel):
    latest_timestamp: str


@router.get(
    "/metadata",
    summary="Metadata of the underlying ohsomedb",
    tags=["Metadata"],
)
async def get_metadata() -> MetadataResponseModel:
    timestamp = await service.get_latest_timestamp()

    return MetadataResponseModel(latest_timestamp=timestamp.isoformat())
