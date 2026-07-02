# How-To Guides

## How to extract features with Python?

How to extract OpenStreetMap data form the ohsome API with Python into a GeoDataFrame?

```python
from io import BytesIO

import geopandas as gpd
import httpx


BASE_URL = "https://api.heigit.org/ohsome-api-staging/v2"
OHSOME_API_URL = BASE_URL + "/features/extraction.parquet"
OHSOME_API_KEY = "your-api-key"


aoi: dict = {
  "filter": "type:node and natural=tree",
  "aoi": {
    "type": "Polygon",
    "coordinates": [
      [
        [
          8.72362,
          49.41582
        ],
        [
          8.68812,
          49.41582
        ],
        [
          8.68812,
          49.4039
        ],
        [
          8.72362,
          49.4039
        ],
        [
          8.72362,
          49.41582
        ]
      ]
    ]
  }
}
osm_filter: str = "type:node and natural=tree"

response = httpx.post(
    OHSOME_API_URL,
    json={"aoi": aoi, "filter": osm_filter},
    headers={"authorization": OHSOME_API_KEY},
)
response.raise_for_status()
buffer = BytesIO(response.content)

features = gpd.read_parquet(
    buffer,
    # Convert parquet maps into python dictionary
    to_pandas_kwargs={"maps_as_pydicts": "strict"},
)

# Extract features from dictionary into columns (explode)
tags = gdf.json_normalize(gdf["osm_tags"])
features_with_tags = features.join(tags).drop("osm_tags", axis="columns")
```
