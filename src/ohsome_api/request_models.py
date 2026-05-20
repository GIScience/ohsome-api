# TODO: return request params in response?
import logging
from datetime import datetime, timedelta, timezone
from typing import Self

from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    field_validator,
    model_validator,
)

logger = logging.getLogger(__name__)

td_adapter = TypeAdapter(timedelta)


class TimeBins(BaseModel):
    start: datetime = Field(
        example="2026-01-01T00:00:00Z",
        description="""
            Only UTC timestamps are supported.
            Earliest OSM timestamp is 2007-10-08T00:00:00Z.
            """,
        ge=datetime(2007, 10, 8),
    )
    end: datetime = Field(
        example="2026-04-17T00:00:00Z",
        description="Only UTC timestamps are supported.",
    )
    bin_size: str | None = Field(example="P1M", default=None)  # TODO: validate

    model_config = ConfigDict(extra="forbid")

    @model_validator(mode="after")
    def validate_end_greater_than_start(self) -> Self:
        if self.end > self.start:
            return self

        raise ValueError("End timestamp needs to be greater than start timestamp.")

    @field_validator("start", "end")
    @classmethod
    def validate_timezone(cls, value: datetime) -> datetime:
        """Allow only UTC."""
        if value.tzinfo is None:
            return value.replace(tzinfo=timezone.utc)

        if value.tzinfo == timezone.utc:
            return value

        raise ValueError("Only UTC timestamps are supported.")

    @field_validator("bin_size")
    @classmethod
    def validate_bin_size(cls, value: str) -> str:
        # uses Pydantic internal logic to validate as timedelta
        td_adapter.validate_python(value)
        return value


class Parameters(BaseModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        description="""[filter language documentation](
        https://docs.ohsome.org/ohsome-api/v1/filter.html)""",
        example="type:node and natural=tree",
    )
    time: TimeBins = Field(
        description="""[time documentation](
        https://docs.ohsome.org/ohsome-api/v1/time.html)""",
    )

    model_config = ConfigDict(extra="forbid")
