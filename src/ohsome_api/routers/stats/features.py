from datetime import datetime, timedelta
from importlib.metadata import version
from typing import Literal, cast

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import (
    AliasChoices,
    Field,
    TypeAdapter,
    computed_field,
)

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import MeasureEnum, SnapshotColumns
from ohsome_api.request_models import (
    FilterRequestModel,
    GroupByRequestModel,
    MeasureRequestModel,
)
from ohsome_api.request_models.aoi import AoiRequestModel
from ohsome_api.request_models.time import (
    TimeRangeRequestModel,
    TimeSeriesRequestModel,
    Timestamp,
    TimestampLatest,
)
from ohsome_api.response_models import (
    SnapshotColumnsResponseModel,
    SnapshotsResponseModel,
)
from ohsome_api.response_renderers import (
    CSV_RESPONSE_DESCRIPTION,
    CSV_SNAPSHOT_EXAMPLE,
    CSVSnapshotsResponse,
)

td_adapter = TypeAdapter(timedelta)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


class StatsFeaturesRequestModel(
    AoiRequestModel,
    FilterRequestModel,
    GroupByRequestModel,
):
    time: TimeSeriesRequestModel | Timestamp | TimestampLatest = Field(
        validation_alias=AliasChoices("time", "timeSeries"),
    )
    clip: bool = Field(
        default=False,
        description=(
            "If true, length or area will be based on the clipped feature geometries. "
            "Depending on your ohsome filter, clipping feature geometryes for large "
            "AOIs can be very expensive (and is usually not needed)."
        ),
    )

    @computed_field
    @property
    def start(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return cast(datetime | Literal["latest"], self.time.start)
        return self.time

    @computed_field
    @property
    def end(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return self.time.end
        return self.time

    @computed_field
    @property
    def interval(self) -> str | None:
        if isinstance(self.time, TimeSeriesRequestModel):
            return self.time.interval
        return None


@router.post(
    "/stats/features/{measure}.json",
    response_class=JSONResponse,
    response_model=SnapshotColumnsResponseModel,
    response_model_exclude_none=True,
    summary="Aggregate features by {measure} as time series.",
    description=(
        "Nodes, ways, and relations tagged as `type=multipolygon` "
        "or `type=boundary` are included. "
        "You can not derive statistics for all other relations."
    ),
    tags=["Statistics"],
)
async def post_features_as_json(
    parameters: StatsFeaturesRequestModel,
    measure: MeasureRequestModel,
) -> dict[str, SnapshotColumns]:
    return {
        "result": await service.get_features_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.start),
            end=parameters.end,
            interval=parameters.interval,
            aoi_wkt=parameters.aoi_wkt,
            measure=cast(MeasureEnum, measure),
            group_by=parameters.group_by,
        )
    }


@router.post(
    "/stats/features/{measure}.csv",
    response_class=CSVSnapshotsResponse,
    response_model=SnapshotsResponseModel,
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": CSV_SNAPSHOT_EXAMPLE,
                },
            },
        },
    },
    summary="Aggregate features by {measure} as time series.",
    description=CSV_RESPONSE_DESCRIPTION,
    tags=["Statistics"],
)
async def post_features_as_csv(
    parameters: StatsFeaturesRequestModel,
    measure: MeasureRequestModel,
) -> dict[str, list]:
    return {
        "result": await service.get_features_rows(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.start),
            end=parameters.end,
            interval=parameters.interval,
            aoi_wkt=parameters.aoi_wkt,
            measure=cast(MeasureEnum, measure),
            group_by=parameters.group_by,
        )
    }
