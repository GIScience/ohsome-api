# TODO: return request params in response?
import logging
from importlib.metadata import version

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse
from ohsome_filter_to_sql import OhsomeFilter
from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel

from ohsome_api import service

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
    api_version: str = version("ohsome-api")
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


class QueryParameters(BaseModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        description="""[filter language documentation](
        https://docs.ohsome.org/ohsome-api/v1/filter.html)""",
        example="type:node and natural=tree",
    )


@app.post("/contributions/count.json", response_class=JSONResponse)
async def get_contributions_count_as_json(
    query_parameters: QueryParameters,
) -> CountResponseModel:
    result = await service.get_contributions_count(**query_parameters.model_dump())
    return CountResponseModel(result=result)


@app.post("/contributions/count.csv", response_class=CSVResponse)
async def get_contributions_count_as_csv(
    query_parameters: QueryParameters,
) -> CountResponseModel:
    result = await service.get_contributions_count(**query_parameters.model_dump())
    return CountResponseModel(result=result)
