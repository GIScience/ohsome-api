from datetime import datetime

from pydantic import BaseModel


class RowModel(BaseModel):
    value: int
    start: datetime
    end: datetime
