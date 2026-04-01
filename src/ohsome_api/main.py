import logging
from importlib.metadata import version

from fastapi import FastAPI
from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

from ohsome_api import service

app = FastAPI()
logger = logging.getLogger(__name__)


class Attribution(BaseModel):
    url: str = "https://ohsome.org/copyrights"
    text: str = "© OpenStreetMap contributors"


class BaseResponse(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    api_version: str = version("ohsome-api")
    attribution: Attribution = Attribution()


class MetadataResponse(BaseResponse):
    latest_timestamp: str


@app.get("/metadata")
async def get_metadata() -> MetadataResponse:
    """Metadata of the underlying ohsomedb."""
    logger.info("Get metadata from ohsomedb.")
    timestamp = service.get_latest_timestamp()

    return MetadataResponse(latest_timestamp=timestamp.isoformat())


# TODO: return request params in response?
# TODO: make CSV response type
@app.get("/contributions/count")
async def get_contributions_count() -> dict:
    result = service.get_contributions_count()
    return {
        "apiVersion": version("ohsome-api"),
        "result": result,
    }
