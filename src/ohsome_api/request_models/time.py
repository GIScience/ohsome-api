from datetime import datetime, timedelta, timezone
from typing import Annotated, Literal, Self

from pydantic import (
    AfterValidator,
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    field_validator,
    model_validator,
)
from pydantic.alias_generators import to_camel

from ohsome_api.config import CONFIG

td_adapter = TypeAdapter(timedelta)


def validate_timezone(
    value: datetime | Literal["latest"],
) -> datetime | Literal["latest"]:
    if value == "latest":
        return value

    # Allow only UTC.
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)

    if value.tzinfo == timezone.utc:
        return value

    raise ValueError("Only UTC timestamps are supported.")


def validate_timestamp(
    value: datetime | Literal["latest"],
) -> datetime | Literal["latest"]:
    if value == "latest":
        return value

    if value >= datetime(2007, 10, 8, tzinfo=timezone.utc):
        return value

    raise ValueError("Timestamps needs to be greater or equal then 2007-10-08.")


def transform_earliest_to_timestamp(
    value: datetime | Literal["earliest"],
) -> datetime:
    if value == "earliest":
        return datetime(2007, 10, 8, tzinfo=timezone.utc)

    return value


def transform_time_timerange(value: str) -> TimeRangeRequestModel:
    parts = value.split("/")
    if len(parts) == 2 and all(parts):
        return TimeRangeRequestModel(start=parts[0], end=parts[1])
    raise ValueError("Invalid time range format.")


class RequestConfigModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
    )


# TODO: Validate timestamps
Timestamp = Annotated[
    datetime,
    Field(
        title="Timestamp",
        description="Single timestamp (ISO-8601, UTC).",
        json_schema_extra={"example": "2026-01-01T00:00:00Z"},
    ),
    AfterValidator(validate_timezone),
    AfterValidator(validate_timestamp),
]

TimestampEarliest = Annotated[
    Literal["earliest"],
    Field(
        title="Earliest Timestamp",
        description="Earliest timestamp is 2007-10-08T00:00:00Z.",
        json_schema_extra={"example": "earliest"},
    ),
    AfterValidator(transform_earliest_to_timestamp),
]

TimestampLatest = Annotated[
    Literal["latest"],
    Field(
        title="Latest Timestamp",
        description="Most recent data.",
        json_schema_extra={"example": "latest"},
    ),
]

TimeRangeStr = Annotated[
    str,
    Field(
        title="Time Range",
        description=(
            "Time range defined using a start/end timestamp (ISO-8601, UTC). "
            "Please take a look at the "
            f"[documentation]({CONFIG.external_docs_url}/reference.html#time)."
        ),
        json_schema_extra={"example": "2025-01-01T00:00:00Z/2026-01-01T00:00:00Z"},
    ),
    AfterValidator(transform_time_timerange),
]


class TimeRangeRequestModel(RequestConfigModel):
    start: Timestamp | TimestampEarliest = Field(
        json_schema_extra={"example": "2025-01-01T00:00:00Z"},
    )
    end: Timestamp | TimestampLatest = Field(
        json_schema_extra={"example": "2026-01-01T00:00:00Z"},
    )

    @model_validator(mode="after")
    def validate_end_greater_than_start(self) -> Self:
        assert isinstance(self.start, datetime)  # noqa: S101

        if self.end == "latest":
            return self

        if self.end > self.start:
            return self

        raise ValueError("End timestamp needs to be greater than start timestamp.")

    model_config = ConfigDict(title="Time Range")


class TimeBinsRequestModel(TimeRangeRequestModel):
    bin_size: str | None = Field(
        default=None,
        description="Bin size (ISO-8601 duration).",
        json_schema_extra={"example": "P1M"},
    )

    @field_validator("bin_size")
    @classmethod
    def validate_bin_size(cls, value: str | None) -> str | None:
        if value is not None:
            # uses Pydantic internal logic to validate as timedelta
            td_adapter.validate_python(value)
        return value

    model_config = ConfigDict(
        title="Time Bins",
        json_schema_extra={
            "description": (
                "Time bins defined using a start/end timestamp (ISO-8601, UTC) "
                "and a bin size (ISO-8601 duration). "
                "Last bin might not cover bin size. Please take a look at the "
                f"[documentation]({CONFIG.external_docs_url}/reference.html#time)."
            ),
        },
    )


class TimeSeriesRequestModel(TimeRangeRequestModel):
    interval: str | None = Field(
        default=None,
        description="Interval (ISO-8601 duration).",
        json_schema_extra={"example": "P1M"},
    )

    @field_validator("interval")
    @classmethod
    def validate_interval(cls, value: str | None) -> str | None:
        if value is not None:
            # uses Pydantic internal logic to validate as timedelta
            td_adapter.validate_python(value)
        return value

    model_config = ConfigDict(
        title="Time Series",
        json_schema_extra={
            "description": (
                "Time series defined using a start/end timestamp (ISO-8601, UTC) "
                "and a interval (ISO-8601 duration). The interval between the last "
                "two timestamp might not fit given duration. Please take a look at the "
                f"[documentation]({CONFIG.external_docs_url}/reference.html#time)."
            ),
        },
    )
