from ohsome_api.db.metadata.metadata import get_metadata
from ohsome_api.models import Metadata


async def get_ohsomedb_metadata() -> Metadata:
    metadata = await get_metadata()
    return Metadata(**metadata)
