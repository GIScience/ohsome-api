from datetime import datetime

from pydantic import BaseModel


# TODO: find common name scheme for row and response models
class TimeBinsRowModel(BaseModel):
    value: int
    start: datetime
    end: datetime


class FeaturesRowModel(BaseModel):
    value: int
    timestamp: datetime
