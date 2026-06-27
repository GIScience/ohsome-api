import os
from io import BytesIO

import geopandas
import httpx
import pandas

BASE_URL = "https://api.heigit.org/ohsome-api-staging/v2"
OHSOME_API_URL = BASE_URL + "/features/extraction.parquet"
OHSOME_API_KEY = os.environ["OHSOME_API_KEY"]


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

response = httpx.post(
    OHSOME_API_URL,
    json={"aoi": aoi, "filter": osm_filter},
    headers={"authorization": OHSOME_API_KEY},
)
response.raise_for_status()
buffer = BytesIO(response.content)

features = geopandas.read_parquet(
    buffer,
    # Convert parquet maps into python dictionary
    to_pandas_kwargs={"maps_as_pydicts": "strict"},
)
print(features.info())

# Extract features from dictionary into columns (explode)
tags = pandas.json_normalize(features["osm_tags"])
features_with_tags = features.join(tags).drop("osm_tags", axis="columns")
print(features_with_tags.info())
