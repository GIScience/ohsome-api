from datetime import datetime
from typing import ClassVar, Literal

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import (
    ConfigDict,
    computed_field,
)

from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import Measure, TimeSeriesGroupedByResult, TimeSeriesResult
from ohsome_api.request_models import (
    ClipRequestModel,
    FilterRequestModel,
    GroupByRequestModel,
)
from ohsome_api.request_models.aoi import AoiRequestModel
from ohsome_api.request_models.time import (
    TimeRange,
    TimeSeries,
    Timestamp,
    TimestampLatest,
)
from ohsome_api.response_models import BaseResponseModel
from ohsome_api.response_renderers import CSVSnapshotsResponse
from ohsome_api.service.stats import features

router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


class StatsFeaturesRequest(
    AoiRequestModel,
    FilterRequestModel,
    GroupByRequestModel,
    ClipRequestModel,
):
    time: TimeSeries | Timestamp | TimestampLatest

    @computed_field
    @property
    def start(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRange):
            return self.time.start
        return self.time

    @computed_field
    @property
    def end(self) -> datetime | Literal["latest"]:
        if isinstance(self.time, TimeRange):
            return self.time.end
        return self.time

    @computed_field
    @property
    def interval(self) -> str | None:
        if isinstance(self.time, TimeSeries):
            return self.time.interval
        return None

    model_config: ClassVar[ConfigDict] = {
        "json_schema_extra": {
            "examples": [
                # Same as example values for individual parameters but without group_by
                {
                    "aoi": [8.68812, 49.4039, 8.72362, 49.41582],
                    "filter": "geometry:point and natural=tree",
                    "time": {
                        "start": "2025-01-01T00:00:00Z",
                        "end": "2026-01-01T00:00:00Z",
                        "interval": "P1M",
                    },
                    "clip": False,
                }
            ]
        }
    }


class StatsFeaturesResponse(BaseResponseModel):
    result: TimeSeriesResult | TimeSeriesGroupedByResult


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
) -> dict[str, TimeSeriesResult]:
    return {
        "result": await features.get_features_columns(
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
        "result": await features.get_features_rows(
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
