# TODO: return request params in response?
from contextlib import asynccontextmanager
from datetime import timedelta
from importlib.metadata import version
from typing import AsyncIterator

import asyncpg
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import (
    TypeAdapter,
)

from ohsome_api.config import CONFIG
from ohsome_api.database import db
from ohsome_api.routers import activity, currentness, docs, features, metadata

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
app.mount(
    "/static",
    StaticFiles(directory="static"),
    name="static",
)
app.mount(
    f"{CONFIG.url_path_prefix}/static",
    StaticFiles(directory="static"),
    name="static",
)
app.include_router(docs.router)
app.include_router(metadata.router)
app.include_router(features.router)
app.include_router(activity.router)
app.include_router(currentness.router)


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
                        "msg": (
                            "Topology Exception occurred while processing request."
                            "Check if input area of interest is valid."
                        ),
                    }
                ]
            },
        )
    raise exception
