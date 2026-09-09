from datetime import datetime, timedelta
from typing import Literal

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import (
    Field,
    TypeAdapter,
    computed_field,
)

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import Measure, SnapshotColumns, SnapshotColumnsGrouped
from ohsome_api.request_models import (
    FilterRequestModel,
    GroupByRequestModel,
)
from ohsome_api.request_models.aoi import AoiRequestModel
from ohsome_api.request_models.time import (
    TimeRangeRequestModel,
    TimeSeriesRequestModel,
    Timestamp,
    TimestampLatest,
)
from ohsome_api.response_models import BaseResponseModel
from ohsome_api.response_renderers import CSVSnapshotsResponse

td_adapter = TypeAdapter(timedelta)

router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


class StatsFeaturesResponse(BaseResponseModel):
    result: SnapshotColumns | SnapshotColumnsGrouped


class StatsFeaturesRequest(
    AoiRequestModel,
    FilterRequestModel,
    GroupByRequestModel,
):
    time: TimeSeriesRequestModel | Timestamp | TimestampLatest
    clip: bool = Field(
        default=False,
        description=(
            "If true, length and area calculations use the clipped feature geometries. "
            "Clipping can be computationally expensive for large AOIs, "
            "depending on your ohsome filter, and is usually unnecessary."
        ),
    )

    @computed_field
    @property
    def start(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRangeRequestModel):
            return self.time.start
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
    response_model=StatsFeaturesResponse,
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
    parameters: StatsFeaturesRequest,
    measure: Measure,
) -> dict[str, SnapshotColumns]:
    return {
        "result": await service.get_features_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=parameters.start,
            end=parameters.end,
            interval=parameters.interval,
            aoi_wkt=parameters.aoi_wkt,
            measure=measure,
            group_by=parameters.group_by,
            clip=parameters.clip,
        )
    }


@router.post(
    "/stats/features/{measure}.csv",
    response_class=CSVSnapshotsResponse,
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": CSVSnapshotsResponse.example,
                },
            },
        },
    },
    summary="Aggregate features by {measure} as time series.",
    description=CSVSnapshotsResponse.description,
    tags=["Statistics"],
)
async def post_features_as_csv(
    parameters: StatsFeaturesRequest,
    measure: Measure,
) -> dict[str, list]:
    return {
        "result": await service.get_features_rows(
            ohsome_filter=parameters.ohsome_filter,
            start=parameters.start,
            end=parameters.end,
            interval=parameters.interval,
            aoi_wkt=parameters.aoi_wkt,
            measure=measure,
            group_by=parameters.group_by,
            clip=parameters.clip,
        )
    }
