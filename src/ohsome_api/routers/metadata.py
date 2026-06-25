from datetime import datetime

from fastapi import APIRouter
from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

from ohsome_api import service
from ohsome_api.response_models import BaseResponseModel

router = APIRouter()


class TemporalExtent(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    latest_timestamp: datetime
    earliest_timestamp: datetime


class MetadataResponseModel(BaseResponseModel):
    temporal_extent: TemporalExtent


@router.get(
    "/metadata",
    summary="Metadata of the underlying ohsomedb",
    tags=["Metadata"],
    response_model=MetadataResponseModel,
)
async def get_metadata() -> dict:
    metadata = await service.get_ohsomedb_metadata()

    return {"temporal_extent": metadata}
