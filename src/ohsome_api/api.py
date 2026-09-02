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

import ohsome_api.routers.extraction.collections
import ohsome_api.routers.extraction.contributions
import ohsome_api.routers.extraction.features
import ohsome_api.routers.filter
import ohsome_api.routers.metadata
import ohsome_api.routers.stats.contributions
import ohsome_api.routers.stats.contributors
import ohsome_api.routers.stats.currentness
import ohsome_api.routers.stats.features
from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.ohsomedb.errors import ResultTooLargeError, TimeSeriesTooLargeError

VERSION = importlib.metadata.version("ohsome-api")
METADATA_PROJECT = importlib.metadata.metadata("ohsome-api")

td_adapter = TypeAdapter(timedelta)

METADATA_TAGS = [
    {"name": "Statistics"},
    {"name": "Statistics (Experimental)"},
    {"name": "Extraction"},
    {"name": "Extraction (Experimental)"},
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
    docs_url=CONFIG.docs_path,
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

app.include_router(ohsome_api.routers.filter.router)
app.include_router(ohsome_api.routers.metadata.router)
app.include_router(ohsome_api.routers.stats.features.router)
app.include_router(ohsome_api.routers.stats.contributors.router)
app.include_router(ohsome_api.routers.stats.contributions.router)
app.include_router(ohsome_api.routers.stats.currentness.router)
app.include_router(ohsome_api.routers.extraction.features.router)
app.include_router(ohsome_api.routers.extraction.collections.router)
app.include_router(ohsome_api.routers.extraction.contributions.router)


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


@app.exception_handler(ResultTooLargeError)
@app.exception_handler(TimeSeriesTooLargeError)
@app.exception_handler(TimeoutError)  # PoolAcquireTimeoutError, QueryTimeoutError
async def timeout_error(
    _: Request,
    error: TimeoutError | ResultTooLargeError | TimeSeriesTooLargeError,
) -> JSONResponse:
    # Asyncpg raises timeouts via asyncio

    # Timeout raised during streaming (/extraction)
    # can not be handled since response has already started.
    return JSONResponse(
        status_code=422,
        content={
            "detail": [
                {
                    "type": type(error).__name__,
                    "msg": str(error),
                }
            ],
        },
    )


class HealthCheck(BaseModel):
    status: str = "Ok"


@app.head("/health", include_in_schema=False)
def head_health() -> None:
    pass


@app.get("/health", summary="Check health.", tags=["Health"])
def get_health() -> HealthCheck:
    return HealthCheck()
