from datetime import datetime
from pathlib import Path

from ohsome_api.database import db

SQL = Path(Path(__file__).parent / "metadata.sql").read_text()


async def get_metadata() -> dict[str, datetime]:
    return dict(await db.fetch_row(SQL))
