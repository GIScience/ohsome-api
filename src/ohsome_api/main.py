import logging
from importlib.metadata import version

from fastapi import FastAPI

from ohsome_api.service import get_latest_timestamp

app = FastAPI()
logger = logging.getLogger(__name__)


@app.get("/metadata")
async def get_metadata() -> dict:
    """Metadata of the underlying ohsomedb."""
    logger.info("Get metadata from ohsomedb.")
    timestamp = get_latest_timestamp()

    return {
        "apiVersion": version("ohsome-api"),
        "latestTimestamp": timestamp.isoformat(),
    }
