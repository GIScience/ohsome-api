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
from starlette.status import (
    HTTP_400_BAD_REQUEST,
    HTTP_401_UNAUTHORIZED,
    HTTP_403_FORBIDDEN,
    HTTP_422_UNPROCESSABLE_CONTENT,
    HTTP_429_TOO_MANY_REQUESTS,
    HTTP_504_GATEWAY_TIMEOUT,
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
from ohsome_api.db.db import db
from ohsome_api.db.errors import OhsomeAPIError, OhsomeApiTimeoutError
from ohsome_api.response_models import (
    HTTPBadRequestError,
    HTTPForbiddenError,
    HTTPGatewayTimeoutError,
    HTTPTooManyRequestsError,
    HTTPUnauthorizedError,
)

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
    responses={
        HTTP_400_BAD_REQUEST: {"model": HTTPBadRequestError},
        HTTP_401_UNAUTHORIZED: {"model": HTTPUnauthorizedError},
        HTTP_403_FORBIDDEN: {"model": HTTPForbiddenError},
        HTTP_429_TOO_MANY_REQUESTS: {"model": HTTPTooManyRequestsError},
        HTTP_504_GATEWAY_TIMEOUT: {"model": HTTPGatewayTimeoutError},
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
async def handle_topology_error(
    request: Request, exception: asyncpg.InternalServerError
) -> JSONResponse:
    if "TopologyException" in str(exception):
        raise exception

    match request.method:
        case "GET":
            loc = ["query", "aoi"]
        case "POST":
            loc = ["body", "aoi"]
        case _:
            raise exception

    return JSONResponse(
        status_code=HTTP_422_UNPROCESSABLE_CONTENT,
        content={
            "detail": [
                {
                    "loc": loc,
                    "type": "topology_exception",
                    "msg": (
                        "Topology Exception occurred while processing request."
                        "Check if input area of interest is valid."
                    ),
                }
            ]
        },
    )


@app.exception_handler(OhsomeApiTimeoutError)
async def handle_timeout_error(
    _: Request, error: OhsomeApiTimeoutError
) -> JSONResponse:
    # Asyncpg raises timeouts via asyncio

    # Timeout raised during streaming (/extraction)
    # can not be handled since response has already started.
    return JSONResponse(
        status_code=HTTP_504_GATEWAY_TIMEOUT,
        content={
            "type": type(error).__name__,
            "error": str(error),
        },
    )


@app.exception_handler(OhsomeAPIError)
async def handle_ohsome_api_error(_: Request, error: OhsomeAPIError) -> JSONResponse:
    return JSONResponse(
        status_code=HTTP_400_BAD_REQUEST,
        content={
            "type": type(error).__name__,
            "error": str(error),
        },
    )


class HealthResponse(BaseModel):
    status: str = "Ok"


@app.head("/health", include_in_schema=False)
def head_health() -> None:
    pass


@app.get("/health", summary="Check health.", tags=["Health"])
def get_health() -> HealthResponse:
    return HealthResponse()
