# TODO: return request params in response?
import logging
from datetime import datetime
from importlib.metadata import version

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse
from ohsome_filter_to_sql import OhsomeFilter
from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel

from ohsome_api import service

VERSION = version("ohsome-api")

app = FastAPI()
logger = logging.getLogger(__name__)


class CSVResponse(Response):
    media_type = "text/csv"

    def render(self, content: dict) -> bytes:
        result = f"""# apiVersion: {content["apiVersion"]}
# attribution.url: {content["attribution"]["url"]}
# attribution.text: {content["attribution"]["text"]}
result
{content["result"]}
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


class CountResponseModel(BaseResponseModel):
    result: int


class Time(BaseModel):
    start: datetime = Field(example="2026-01-01T00:00:00Z")
    end: datetime = Field(example="2026-04-17T00:00:00Z")


class Parameters(BaseModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        description="""[filter language documentation](
        https://docs.ohsome.org/ohsome-api/v1/filter.html)""",
        example="type:node and natural=tree",
    )
    time: Time = Field(
        description="""[time documentation](
        https://docs.ohsome.org/ohsome-api/v1/time.html)""",
    )


@app.post("/contributions/count.json", response_class=JSONResponse)
async def get_contributions_count_as_json(
    parameters: Parameters,
) -> CountResponseModel:
    result = await service.get_contributions_count(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time.start,
        end=parameters.time.end,
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
    )
    return CountResponseModel(result=result)
