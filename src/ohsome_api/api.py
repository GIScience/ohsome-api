# TODO: return request params in response?
import importlib.metadata
from contextlib import asynccontextmanager
from datetime import timedelta
from typing import AsyncIterator

import asyncpg
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pydantic import (
    BaseModel,
    TypeAdapter,
)

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.routers import (
    activity,
    currentness,
    docs,
    extraction,
    features,
    metadata,
)
from ohsome_api.routers import filter as filter_

VERSION = importlib.metadata.version("ohsome-api")
METADATA_PROJECT = importlib.metadata.metadata("ohsome-api")

td_adapter = TypeAdapter(timedelta)

METADATA_TAGS = [
    {"name": "Statistics"},
    {"name": "Extraction"},
    {"name": "Filter Validation"},
    {"name": "Metadata"},
]


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    await db.connect()
    yield
    await db.disconnect()


app = FastAPI(
    root_path=CONFIG.root_path,
    lifespan=lifespan,
    openapi_url="/openapi.json",
    openapi_tags=METADATA_TAGS,
    docs_url=None,  # configured in routers/docs.py
    redoc_url=None,
    version=VERSION,
    title=METADATA_PROJECT["Name"],
    description=(
        METADATA_PROJECT["Summary"]
        + f'<p><a class="link" href="{CONFIG.external_docs_url}">'
        + "Read the documentation.</a></p>"
    ),
    contact={
        "name": METADATA_PROJECT["Author"],
        "url": "https://heigit.org/big-spatial-data-analytics/",
        "email": METADATA_PROJECT["Author-email"],
    },
    license_info={
        "name": "GNU Affero General Public License",
        "url": "https://www.gnu.org/licenses/agpl-3.0.en.html",
    },
)

app.include_router(docs.router)
app.include_router(filter_.router)
app.include_router(metadata.router)
app.include_router(features.router)
app.include_router(activity.router)
app.include_router(currentness.router)
app.include_router(extraction.router)


@app.exception_handler(asyncpg.InternalServerError)
async def postgres_internal_server_error_handler(
    _: Request, exception: asyncpg.InternalServerError
) -> JSONResponse:
    msg = str(exception)
    if "TopologyException" in msg:
        return JSONResponse(
            status_code=422,
            content={
                "detail": [
                    {
                        "type": "topology_exception",
                        "msg": (
                            "Topology Exception occurred while processing request."
                            "Check if input area of interest is valid."
                        ),
                    }
                ]
            },
        )
    raise exception


class HealthCheck(BaseModel):
    status: str = "Ok"


@app.head("/health", include_in_schema=False)
def head_health() -> None:
    # TODO: actually perform a health check
    pass


@app.get("/health", summary="Check health.", tags=["Health"])
def get_health() -> HealthCheck:
    # TODO: actually perform a health check
    return HealthCheck()
