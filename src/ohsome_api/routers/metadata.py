import asyncio
from typing import AsyncIterator

from fastapi import APIRouter
from fastapi.responses import StreamingResponse

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


@router.get(
    path="/debug_stream",
    response_class=StreamingResponse,
    tags=["Debug"],
    include_in_schema=False,
)
async def debug_stream() -> StreamingResponse:
    return StreamingResponse(data_stream(), media_type="text/plain")


async def data_stream() -> AsyncIterator[str]:
    yield "Chunk 1: Processing...\n"
    await asyncio.sleep(1)
    yield "Chunk 2: Processing...\n"

    # Simulate mid-stream cancellation or failure condition
    should_cancel = True
    if should_cancel:
        # Do NOT catch this inside the generator.
        # Let it propagate to Uvicorn to break the TCP socket.
        raise TimeoutError("Stream abruptly canceled")
