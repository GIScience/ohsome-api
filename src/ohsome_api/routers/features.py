import csv
from importlib.metadata import version
from io import StringIO
from typing import AsyncIterator

from fastapi import APIRouter, Depends, Response
from fastapi.responses import JSONResponse, StreamingResponse

from ohsome_api import service
from ohsome_api.dependencies import api_key_header_scheme
from ohsome_api.models import FeaturesRowModel
from ohsome_api.parquet import AsyncParquetSink
from ohsome_api.request_models import BaseParameters, Measure, TimeSeriesParameters
from ohsome_api.response_models import BaseResponseModel

VERSION = version("ohsome-api")
router = APIRouter(
    dependencies=[Depends(api_key_header_scheme)],
)


class FeaturesResponseModel(BaseResponseModel):
    result: list[FeaturesRowModel]


class CSVFeatureResponse(Response):
    media_type = "text/csv"

    def render(self, content: dict) -> bytes:
        csvfile = StringIO()
        writer = csv.writer(csvfile, delimiter=";", lineterminator="\n")
        comment = [
            [f"# apiVersion: {content['apiVersion']}"],
            [f"# attribution.url: {content['attribution']['url']}"],
            [f"# attribution.text: {content['attribution']['text']}"],
        ]
        header = ["timestamp", "value"]
        rows = [
            (
                r["timestamp"],
                r["value"],
            )
            for r in content["result"]
        ]
        writer.writerows(comment)
        writer.writerow(header)
        writer.writerows(rows)
        return csvfile.getvalue().encode()


@router.post("/features/{measure}.json", response_class=JSONResponse)
async def post_features_as_json(
    parameters: TimeSeriesParameters,
    measure: Measure,
) -> FeaturesResponseModel:
    result = await service.get_features(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_series.start,
        end=parameters.time_series.end,
        interval=parameters.time_series.interval,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return FeaturesResponseModel(result=result)


@router.post(
    "/features/{measure}.csv",
    response_class=CSVFeatureResponse,
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
timestamp;result
1970-01-01T00:00:00Z;0
""",
                },
            },
        },
    },
)
async def post_features_as_csv(
    parameters: TimeSeriesParameters,
    measure: Measure,
) -> FeaturesResponseModel:
    result = await service.get_features(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_series.start,
        end=parameters.time_series.end,
        interval=parameters.time_series.interval,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return FeaturesResponseModel(result=result)


# TODO: Address complexity
@router.post("/features/extraction.parquet", response_class=StreamingResponse)
async def post_contributions_extract(  # noqa: C901
    parameters: BaseParameters,
) -> StreamingResponse:
    # TODO: if request is aborted producer should also cancel

    # Database result is written to sink batch wise
    sink = AsyncParquetSink()

    async def stream() -> AsyncIterator[bytes]:
        producer = service.get_extracted_features(
            parameters.ohsome_filter,
            parameters.aoi.features[0].geometry.wkt,
        )
        async for batch in producer:
            sink.write_batch(batch)
            for chunk in sink.io.fetch_all():
                yield chunk

        sink.close()
        for chunk in sink.io.fetch_all():
            yield chunk

    return StreamingResponse(
        stream(),
        media_type="application/vnd.apache.parquet",
        headers={"Content-Disposition": 'attachment; filename="extractions.parquet"'},
    )
