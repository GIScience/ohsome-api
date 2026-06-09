import csv
from importlib.metadata import version
from io import StringIO

from fastapi import APIRouter, Response
from fastapi.responses import JSONResponse

from ohsome_api import service
from ohsome_api.request_models import Measure, TimeBinsParameters
from ohsome_api.response_models import CountResponseModel

VERSION = version("ohsome-api")

router = APIRouter()


class CSVResponse(Response):
    media_type = "text/csv"

    def render(self, content: dict) -> bytes:
        csvfile = StringIO()
        writer = csv.writer(csvfile, lineterminator="\n")
        comment = [
            [f"# apiVersion: {content['apiVersion']}"],
            [f"# attribution.url: {content['attribution']['url']}"],
            [f"# attribution.text: {content['attribution']['text']}"],
        ]
        header = ["start", "end", "value"]
        rows = [
            (
                r["start"],
                r["end"],
                r["value"],
            )
            for r in content["result"]
        ]
        writer.writerows(comment)
        writer.writerow(header)
        writer.writerows(rows)
        return csvfile.getvalue().encode()


@router.post("/currentness/{measure}.json", response_class=JSONResponse)
async def post_currentness_as_json(
    parameters: TimeBinsParameters,
    measure: Measure,
) -> CountResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return CountResponseModel(result=result)


@router.post(
    "/currentness/{measure}.csv",
    response_class=CSVResponse,
    responses={
        200: {
            "content": {
                "text/csv": {
                    "schema": {"type": "string"},
                    "example": f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
result
0
""",
                },
            },
        },
    },
)
async def post_currentness_as_csv(
    parameters: TimeBinsParameters,
    measure: Measure,
) -> CountResponseModel:
    result = await service.get_currentness(
        ohsome_filter=parameters.ohsome_filter,
        start=parameters.time_bins.start,
        end=parameters.time_bins.end,
        bin_size=parameters.time_bins.bin_size,
        aoi_wkt=parameters.aoi.features[0].geometry.wkt,
        measure=measure,
    )
    return CountResponseModel(result=result)
