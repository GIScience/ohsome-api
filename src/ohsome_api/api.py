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
from ohsome_api.request_models import Measure, Parameters

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


@app.post("/currentness/{measure}.json", response_class=JSONResponse)
async def get_contributions_count_as_json(
    parameters: Parameters,
    measure: Measure,
) -> CountResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return CountResponseModel(result=result)


@app.post(
    "/currentness/{measure}.csv",
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
async def get_currentness_as_csv(
    parameters: Parameters,
    measure: Measure,
) -> CountResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return CountResponseModel(result=result)
