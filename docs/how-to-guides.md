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


class OhsomeError(Exception):
    pass


def features_extraction(
    aoi: dict,
    osm_filter: str,
) -> gpd.GeoDataFrame:
    response = httpx.post(
        OHSOME_API_URL,
        json={"aoi": aoi, "filter": osm_filter},
        headers={"authorization": OHSOME_API_KEY},
    )
    try:
        response.raise_for_status()
    except httpx.HTTPStatusError as error:
        raise OhsomeError(response.json()) from error

    buffer = BytesIO(response.content)

    gdf = gpd.read_parquet(
        buffer,
        to_pandas_kwargs={"maps_as_pydicts": "strict"},
    )
    gdf_exploded_tags = gdf.json_normalize(gdf["osm_tags"])
    return gdf.join(gdf_exploded_tags).drop("osm_tags", axis="columns")
```
