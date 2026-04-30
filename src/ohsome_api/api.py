# TODO: return request params in response?
import logging
from contextlib import asynccontextmanager
from datetime import datetime, timedelta, timezone
from importlib.metadata import version
from typing import AsyncIterator, Self

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse
from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    field_validator,
    model_validator,
)
from pydantic.alias_generators import to_camel

from ohsome_api import service
from ohsome_api.database import db
from ohsome_api.models import RowModel

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


class Time(BaseModel):
    start: datetime = Field(
        example="2026-01-01T00:00:00Z",
        description="""
            Only UTC timestamps are supported.
            Earliest OSM timestamp is 2007-10-08T00:00:00Z.
            """,
    )
    end: datetime = Field(
        example="2026-04-17T00:00:00Z",
        description="Only UTC timestamps are supported.",
    )
    period: str | None = Field(example="P1M", default=None)  # TODO: validate

    model_config = ConfigDict(extra="forbid")

    @model_validator(mode="after")
    def validate_end_greater_than_start(self) -> Self:
        if self.end > self.start:
            return self

        raise ValueError("End timestamp needs to be greater than start timestamp.")

    @field_validator("start", "end")
    @classmethod
    def validate_timezone(cls, value: datetime) -> datetime:
        """Allow only UTC."""
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)

        if value.tzinfo == timezone.utc:
            return value

        raise ValueError("Only UTC timestamps are supported.")

    @field_validator("period")
    @classmethod
    def validate_period(cls, value: str) -> str:
        # uses Pydantic internal logic to validate as timedelta
        td_adapter.validate_python(value)
        return value


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

    model_config = ConfigDict(extra="forbid")


@app.post("/contributions/count.json", response_class=JSONResponse)
async def get_contributions_count_as_json(
    parameters: Parameters,
) -> CountResponseModel:
    result = await service.get_contributions_count(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time.start,
        end=parameters.time.end,
        period=parameters.time.period,
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
        period=parameters.time.period,
    )
    return CountResponseModel(result=result)
