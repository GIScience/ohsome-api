from datetime import datetime

from pydantic import BaseModel


class RowModel(BaseModel):
    value: int
    start_timestamp: datetime
    end_timestamp: datetime
