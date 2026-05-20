# TODO: return request params in response?
import logging
from contextlib import asynccontextmanager
from datetime import timedelta
from importlib.metadata import version
from typing import AsyncIterator

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse
from pydantic import (
    BaseModel,
    ConfigDict,
    TypeAdapter,
)
from pydantic.alias_generators import to_camel

from ohsome_api import service
from ohsome_api.database import db
from ohsome_api.models import RowModel
from ohsome_api.request_models import Parameters

VERSION = version("ohsome-api")


logger = logging.getLogger(__name__)

td_adapter = TypeAdapter(timedelta)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    await db.connect()
    yield
    await db.disconnect()


app = FastAPI(lifespan=lifespan)


class CSVResponse(Response):
    media_type = "text/csv"

    def render(self, content: dict) -> bytes:
        result = f"""# apiVersion: {content["apiVersion"]}
# attribution.url: {content["attribution"]["url"]}
# attribution.text: {content["attribution"]["text"]}
start,end,value
{content["result"][0]["start"]},{content["result"][0]["end"]},{content["result"][0]["value"]}
"""
        return result.encode()


class Attribution(BaseModel):
    url: str = "https://ohsome.org/copyrights"
    text: str = "© OpenStreetMap contributors"


class BaseResponseModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    api_version: str = VERSION
    attribution: Attribution = Attribution()


class MetadataResponseModel(BaseResponseModel):
    latest_timestamp: str


@app.get("/metadata")
async def get_metadata() -> MetadataResponseModel:
    """Metadata of the underlying ohsomedb."""
    logger.info("Get metadata from ohsomedb.")
    timestamp = await service.get_latest_timestamp()

    return MetadataResponseModel(latest_timestamp=timestamp.isoformat())


# TODO: Rename
class CountResponseModel(BaseResponseModel):
    result: list[RowModel]


@app.post("/contributions/count.json", response_class=JSONResponse)
async def get_contributions_count_as_json(
    parameters: Parameters,
) -> CountResponseModel:
    result = await service.get_contributions_count(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time.start,
        end=parameters.time.end,
        bin_size=parameters.time.bin_size,
    )
    return CountResponseModel(result=result)


@app.post(
    "/contributions/count.csv",
    response_class=CSVResponse,
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
result
0
""",
                },
            },
        },
    },
)
async def get_contributions_count_as_csv(
    parameters: Parameters,
) -> CountResponseModel:
    result = await service.get_contributions_count(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time.start,
        end=parameters.time.end,
        bin_size=parameters.time.bin_size,
    )
    return CountResponseModel(result=result)
