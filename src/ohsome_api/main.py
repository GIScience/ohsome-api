import logging

from fastapi import FastAPI

from ohsome_api.service import get_result

app = FastAPI()
logger = logging.getLogger(__name__)


@app.get("/", name="Hello World endpoint")
async def read_root() -> dict:
    """Returns Hello World as a key/value pair"""
    logger.info("hello world request")
    return {"Hello": get_result()}
