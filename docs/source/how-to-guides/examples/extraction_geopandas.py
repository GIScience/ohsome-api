import os
from io import BytesIO

import geopandas
import httpx
import pandas
from geopandas import GeoDataFrame
from httpx import Response

OHSOME_API_URL = os.environ.get(
    "OHSOME_API_URL",
    "https://api.heigit.org/ohsome-api/v2-rc",
)
OHSOME_API_KEY = os.environ["OHSOME_API_KEY"]


class OhsomeError(Exception):
    pass


def request(path: str, json_body: dict) -> BytesIO:
    response: Response = httpx.post(
        OHSOME_API_URL + path,
        json=json_body,
        headers={"authorization": OHSOME_API_KEY},
    )
    try:
        response.raise_for_status()
    except httpx.HTTPStatusError as error:
        raise OhsomeError("ohsome API request failed.") from error
    return BytesIO(response.content)


def features_extraction(
    aoi: dict,
    osm_filter: str,
    clip: bool = True,
) -> GeoDataFrame:
    path = "/extraction/features.parquet"
    json_body = {
        "aoi": aoi,
        "filter": osm_filter,
        "time": "latest",
        "clip": clip,
    }
    buffer = request(path, json_body)

    # Read as parquet and pass OSM tag maps into dictionaries
    features = geopandas.read_parquet(
        buffer,
        to_pandas_kwargs={"maps_as_pydicts": "strict"},
    )

    # Extract OSM tags from dictionary into columns (explode)
    tags = pandas.json_normalize(features["osm_tags"])
    return features.join(tags).drop("osm_tags", axis="columns")


if __name__ == "__main__":
    aoi: dict = {
        "type": "Polygon",
        "coordinates": [
            [
                [8.72362, 49.41582],
                [8.68812, 49.41582],
                [8.68812, 49.4039],
                [8.72362, 49.4039],
                [8.72362, 49.41582],
            ]
        ],
    }
    osm_filter: str = "type:node and natural=tree"
    features = features_extraction(aoi=aoi, osm_filter=osm_filter)
    print(features.info())
