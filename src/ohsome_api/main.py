import logging
from importlib.metadata import version

from fastapi import FastAPI

from ohsome_api import service

app = FastAPI()
logger = logging.getLogger(__name__)


@app.get("/metadata")
async def get_metadata() -> dict:
    """Metadata of the underlying ohsomedb."""
    logger.info("Get metadata from ohsomedb.")
    timestamp = service.get_latest_timestamp()

    return {
        "apiVersion": version("ohsome-api"),
        "attribution": {
            "url": "https://ohsome.org/copyrights",
            "text": "© OpenStreetMap contributors",
        },
        "latestTimestamp": timestamp.isoformat(),
    }


# TODO: return request params in response?
# TODO: make CSV response type
@app.get("/contributions/count")
async def get_contributions_count() -> dict:
    result = service.get_contributions_count()
    return {
        "apiVersion": version("ohsome-api"),
        "result": result,
    }
