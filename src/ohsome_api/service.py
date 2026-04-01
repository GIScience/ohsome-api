from datetime import datetime

from ohsome_api import db


def get_latest_timestamp() -> datetime:
    return db.get_latest_timestamp()
