import logging

from fastapi import FastAPI

app = FastAPI()
logger = logging.getLogger(__name__)


@app.get("/")
def read_root() -> dict:
    logger.info("hello world request")
    return {"Hello": "World"}
