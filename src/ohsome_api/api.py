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
from fastapi.staticfiles import StaticFiles
from pydantic import (
    TypeAdapter,
)

from ohsome_api import service
from ohsome_api.database import db
from ohsome_api.request_models import Measure, TimeBinsParameters
from ohsome_api.response_models import CountResponseModel
from ohsome_api.routers import activity, docs, features, metadata

VERSION = version("ohsome-api")


td_adapter = TypeAdapter(timedelta)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    await db.connect()
    yield
    await db.disconnect()


app = FastAPI(
    lifespan=lifespan,
    docs_url=None,
    redoc_url=None,
    version=VERSION,
    title="ohsome-api",
    contact={
        "name": "HeiGIT gGmbH",
        "email": "ohsome@heigit.org",
    },
)
app.mount("/static", StaticFiles(directory="static"), name="static")
app.include_router(docs.router)
app.include_router(metadata.router)
app.include_router(features.router)
app.include_router(activity.router)


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
