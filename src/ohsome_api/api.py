# TODO: return request params in response?
# TODO: rename post function def from get_... to post_...
# TODO: split file into smaller files (FastAPI router?)
import csv
from contextlib import asynccontextmanager
from datetime import timedelta
from importlib.metadata import version
from io import StringIO
from typing import AsyncIterator

import asyncpg
from fastapi import FastAPI, Request, Response
from fastapi.responses import JSONResponse
from pydantic import (
    BaseModel,
    ConfigDict,
    TypeAdapter,
)
from pydantic.alias_generators import to_camel

from ohsome_api import service
from ohsome_api.database import db
from ohsome_api.models import FeaturesRowModel, TimeBinsRowModel
from ohsome_api.request_models import Measure, TimeBinsParameters, TimeSeriesParameters

VERSION = version("ohsome-api")


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
        csvfile = StringIO()
        writer = csv.writer(csvfile, lineterminator="\n")
        comment = [
            [f"# apiVersion: {content['apiVersion']}"],
            [f"# attribution.url: {content['attribution']['url']}"],
            [f"# attribution.text: {content['attribution']['text']}"],
        ]
        header = ["start", "end", "value"]
        rows = [
            (
                r["start"],
                r["end"],
                r["value"],
            )
            for r in content["result"]
        ]
        writer.writerows(comment)
        writer.writerow(header)
        writer.writerows(rows)
        return csvfile.getvalue().encode()


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
    timestamp = await service.get_latest_timestamp()

    return MetadataResponseModel(latest_timestamp=timestamp.isoformat())


# TODO: Rename
class CountResponseModel(BaseResponseModel):
    result: list[TimeBinsRowModel]


class FeaturesResponseModel(BaseResponseModel):
    result: list[FeaturesRowModel]


@app.exception_handler(asyncpg.InternalServerError)
async def postgres_internal_server_error_handler(
    request: Request, exception: asyncpg.InternalServerError
) -> JSONResponse:
    msg = str(exception)
    if "TopologyException" in msg:
        return JSONResponse(
            status_code=422,
            content={
                "detail": [
                    {
                        "type": "topology_exception",
                        "msg": """Topology Exception occurred while processing request.
            Check if input area of interest is valid.""",
                    }
                ]
            },
        )
    raise exception


# TODO: Rename to currentness
@app.post("/currentness/{measure}.json", response_class=JSONResponse)
async def post_contributions_count_as_json(
    parameters: TimeBinsParameters,
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
async def post_currentness_as_csv(
    parameters: TimeBinsParameters,
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


@app.post("/activity/users.json", response_class=JSONResponse)
async def post_users_activity_as_json(
    parameters: TimeBinsParameters,
) -> CountResponseModel:
    result = await service.get_users_activity(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
    )

    return CountResponseModel(result=result)


@app.post("/features/{measure}.json", response_class=JSONResponse)
async def post_features_as_json(
    parameters: TimeSeriesParameters,
    measure: Measure,
) -> FeaturesResponseModel:
    result = await service.get_features(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_series.start,
        end=parameters.time_series.end,
        interval=parameters.time_series.interval,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return FeaturesResponseModel(result=result)
