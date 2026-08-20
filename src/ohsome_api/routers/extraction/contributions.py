from datetime import datetime
from importlib.metadata import version
from typing import Annotated, Literal

from fastapi import APIRouter, Depends, Query
from fastapi.responses import StreamingResponse
from pydantic import (
    computed_field,
)

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.request_models import FilterRequestModel
from ohsome_api.request_models.aoi import AoiQueryModel, AoiRequestModel
from ohsome_api.request_models.time import (
    TimeRangeRequestModel,
    TimeRangeStr,
    transform_time_timerange,
)

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)

CONTRIBUTIONS_EXTRACT_DESCRIPTION = (
    "Nodes, ways, and relations tagged as `type=multipolygon` or `type=boundary` "
    "are included. "
    "Other relations can be queried with the `/extraction/collections.*` endpoints."
)


class ContributionsExtractionRequestParametersModel(
    AoiRequestModel,
    FilterRequestModel,
):
    time: TimeRangeRequestModel

    @computed_field
    @property
    def start(self) -> datetime:
        return self.time.start

    @computed_field
    @property
    def end(self) -> datetime | Literal["latest"]:
        return self.time.end


class ContributionsExtractionQueryParametersModel(
    AoiQueryModel,
    FilterRequestModel,
):
    time: TimeRangeStr

    @computed_field
    @property
    def start(self) -> datetime:
        return transform_time_timerange(self.time).start

    @computed_field
    @property
    def end(self) -> datetime | Literal["latest"]:
        return transform_time_timerange(self.time).end


@router.post(
    path="/extraction/contributions.parquet",
    response_class=StreamingResponse,
    summary="Download contributions.",
    description=CONTRIBUTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction (Experimental)"],
)
async def post_contributions_extract(
    parameters: ContributionsExtractionRequestParametersModel,
) -> StreamingResponse:
    return await contributions_extract(parameters)


@router.get(
    path="/extraction/contributions.parquet",
    response_class=StreamingResponse,
    summary="Download contributions.",
    description=CONTRIBUTIONS_EXTRACT_DESCRIPTION,
    tags=["Extraction (Experimental)"],
)
async def get_contributions_extract(
    parameters: Annotated[
        ContributionsExtractionQueryParametersModel,
        Query(),
    ],
) -> StreamingResponse:
    return await contributions_extract(parameters)


async def contributions_extract(
    parameters: ContributionsExtractionRequestParametersModel
    | ContributionsExtractionQueryParametersModel,
) -> StreamingResponse:
    stream = await service.extract_contributions_as_parquet(
        parameters.ohsome_filter,
        parameters.aoi_wkt,
        parameters.start,
        parameters.end,
    )
    return StreamingResponse(
        stream,
        media_type="application/vnd.apache.parquet",
        headers={"Content-Disposition": 'attachment; filename="contributions.parquet"'},
    )
