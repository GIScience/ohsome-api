from datetime import datetime
from importlib.metadata import version
from typing import cast

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import MeasureEnum, SnapshotColumns
from ohsome_api.request_models import (
    MeasureRequestModel,
    TimeSeriesRequestParametersModel,
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

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


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
    parameters: TimeSeriesRequestParametersModel,
    measure: MeasureRequestModel,
) -> dict[str, SnapshotColumns]:
    return {
        "result": await service.get_features_columns(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time_series.start),
            end=parameters.time_series.end,
            interval=parameters.time_series.interval,
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
    parameters: TimeSeriesRequestParametersModel,
    measure: MeasureRequestModel,
) -> dict[str, list]:
    return {
        "result": await service.get_features_rows(
            ohsome_filter=parameters.ohsome_filter,
            start=cast(datetime, parameters.time_series.start),
            end=parameters.time_series.end,
            interval=parameters.time_series.interval,
            aoi_wkt=parameters.aoi_wkt,
            measure=cast(MeasureEnum, measure),
        )
    }
