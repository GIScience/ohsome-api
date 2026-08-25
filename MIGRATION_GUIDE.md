# Migrate from v1

Version 2 of the ohsome API is a complete rewrite of the ohsome API functionality, implemented in Python, and further also using a new database backed.

The core idea remained the same, which is to allow you to inspect the history of OpenStreetMap data in a flexible manner by generating statistics of the data or by downloading data extracts. The new database backend of v2 allows us to also ship data updates more frequently, meaning that the results are more closely reflecting the current state of the OpenStreetMap data.

For v2, we tried to consolidate the large amount of endpoints and parameters of the previous version of the ohsome API to a condenset set that is more comprehensible and easy to understand. At the same time we want to continue to support existing functionality that was previously frequently requested. For this we changed the API structure in a couple of way, as explained below. Some new functionality is also intruced in this version, some notable examples are also included in the tables below.

If you are missing a particular feature in v2 that you used in the previous version of the ohsome API, or a completely new feature, please feel free to [reach out](mailto:ohsome@heigit.org) with your use case or open an [issue](https://github.com/GIScience/ohsome-api/issues/new/choose) on github.

## API Key

One major change compared to the initial prototypes of the ohsome API is as of v2, it is required to supply an API key with every request. You can get an API key for free by [signing up here](https://account.heigit.org/signup) to access the ohsome API. If you should need a larger quota than the free API tier allows, please [contact us](mailto:ohsome@heigit.org) with a description of your use case.


The URLs for the ohsome API hav2 changed:

| `v1` | `v2` | comment |
| ---- | ---- | ------- |
| `https://api.ohsome.org/v1/` | `https://api.heigit.org/ohsome-api/v2-rc/`[^0] | API endpoints root URL |
| `https://api.ohsome.org/v1/swagger-ui.html` | `https://api.heigit.org/ohsome-api/v2-rc/docs`[^0] | API documentation |
| `https://docs.ohsome.org/ohsome-api/v1/` | `https://docs.ohsome.org/ohsome-api/v2-rc/`[^0] | general reference documentation, how-to guides and explanations |


> [!IMPORTANT]
> The `/v1/` API endpoints will be shut down on November 30, 2026.

[^0]: At a later point in time, the URLs will be changed to omit the `-rc` part, e.g. the root URL for the API endpoints will be finally `https://api.heigit.org/ohsome-api/v2/`.

## Changed API Endpoints

The following `v1` endpoints have a direct correspondence in the ohsome API version 2:

### General

Most endpoints now include the response file format as a suffix similar to a “filename extension”. For example, a statistics endpoint ending in `.json` will return JSON data. See below which formats are 

### Statistics / Aggregation Endpoints

#### Paths

You find these now consistently under the `/stats` directory:

| `v1` | `v2` |
| ---- | ---- |
| `/v1/elements/count` | `/v2/stats/features/count`[^1] |
| `/v1/elements/length` | `/v2/stats/features/length` |
| `/v1/elements/area` | `/v2/stats/features/area` |
| `/v1/elements/perimeter` | not available in `v2` |
| `/v1/contributions/count` | `/v2/stats/contributions/count` |
| `/v1/contributions/latest/count` | `/v2/stats/currentness/count` |
| `/v1/users/count` | `/v2/stats/contributors/count` |
| `/v1/…/density` | not available, can be calculated on client side |
| `/v1/…/ratio` | not available, can be acchieved by performing two requests and calculating the ratio on client side |
| `/v1/…/groupBy/tag` | see request parameter `groupBy` below |
| `/v1/users/count/groupBy/tag` | not available |
| `/v1/…/groupBy/key` | not available, instead perform one query for each key |
| `/v1/…/groupBy/boundary` | not available, instead perform one query for each area of interest |
| `/v1/…/groupBy/type` | not available, instead perform one query for each type |

[^1]: This endpoint does not include relations that are not representing a polygon (i.e. a `type=multipolygon` or `type=boundary`). For generating statistics about other relations types, we might include a `stats/collections/count` endpoint in an upcoming update.

#### Request Parameters

All of the endpoints are now only available as `POST` requests, with a JSON body payload instead of the previous `x-www-form-urlencoded` parameters.

| `v1` | `v2` | example |
| ---- | ---- | ------- |
| `bboxes` | `aoi` | <pre lang="json">"aoi": [&#13;8.68812,&#13;    49.4039,&#13;    8.72362,&#13;    49.41582&#13;]</pre> |
| `bpolys` | `aoi`[^2] | <pre lang="json">"aoi": {&#13;    "type": "Polygon",&#13;    "coordinates": […]&#13;}</pre> |
| `bcircles` | N/A | |
| `filter` | `filter` | <pre lang="json">"filter": "natural=tree"</pre> |
| `format` | N/A, this is part of the path (see below) | |
| `showMetadata` | N/A | |
| `time` | `time`[^3] | <pre lang="json">"time": {&#13;    "start": "…",&#13;    "end": "…",&#13;    "interval": "…"&#13;}</pre> | |
| `timeout` | N/A | |
| N/A | `groupBy` | <pre lang="json">"groupBy": {&#13;    "type": "byTag",&#13;    "key": "…"&#13;}</pre> |
| N/A | `clip`[^4] | <pre lang="json">"clip": true</pre> |

[^2]: instead of a full FeatureCollection, `v2` accepts a single GeoJSON Geometry object of a `Polygon` or `MultiPolygon` only.
[^3]: `v1` also supported a list of timestamps with potentially uneven intervals, which is not yet available in `v2`. For such request, multiple individual requests can be performed instead.
[^4]: in `v1` all results from `length` and `area` calculations used values based of the OSM features' geometry clipped to the request's area of interest. `v2` does by default not perform this clipping. For request around relatively small areas, it is recommended to specify `"clip": true` in order to get precise results.

#### Result Formats

The result formats `.json` and `.csv` are supported as suffixes in the path, e.g. `/stats/features/length.json` or `/stats/contributors/count.csv`.

**JSON** responses now use a columnar format:

<table>
<tr><td><pre>v1</pre></td><td><pre>v2</pre></td></tr>
<tr>
<td>

```json
{
  "apiVersion": "1.10.4",
  "attribution": {
    "url": "https://ohsome.org/copyrights",
    "text": "© OpenStreetMap contributors"
  },
  "result": [
    {
      "timestamp": "2025-01-01T00:00:00Z",
      "value": 26856
    },
    …
    {
      "timestamp": "2026-01-01T00:00:00Z",
      "value": 33275
    }
  ]
}
```

</td>
<td>

```json
{
  "apiVersion": "2.0.0",
  "attribution": {
    "url": "https://ohsome.org/copyrights",
    "text": "© OpenStreetMap contributors"
  },
  "result": {
    "timestamp": [
      "2025-01-01T00:00:00Z",
      …
      "2026-01-01T00:00:00Z"
    ],
    "value": [
      26856,
      …
      33275
    ]
  }
}
```

</td>
</tr>
</table>

The **CSV** results have not changed much:

<table>
<tr><td><pre>v1</pre></td><td><pre>v2</pre></td></tr>
<tr>
<td>

```json
# Copyright URL: https://ohsome.org/copyrights
# Copyright Text: © OpenStreetMap contributors
# API Version: 1.10.4
timestamp;value
"2025-01-01T00:00:00Z";"26856"
…
"2026-01-01T00:00:00Z";"33275"
```

</td>
<td>

```csv
# apiVersion: 2.0.0
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
timestamp;value
2025-01-01T00:00:00Z;26856
…
2026-01-01T00:00:00Z;33275
```

</td>
</tr>
</table>

### Extraction Endpoints

#### Paths

| `v1` | `v2` |
| ---- | ---- |
| `/v1/elements/geometry` | `/v2/extraction/features` |
| `/v1/elementsFullHistory/geometry` | `/v2/extraction/features` |
| `/v1/contributions/geometry` | `/v2/extraction/contributions`[^7] |
| `/v1/…/bbox` | N/A (bbox is always included in result along side full geometry) |
| `/v1/…/centroid` | not yet implemented, can be calculated on client side in post-processing |
| N/A | `/v2/extraction/collections`[^6] |
| N/A | `/v2/extraction/collections_members`[^6] |

[^6]: OSM relations that are not a `type=multipolygon` or `type=boundary` (e.g. OSM route relations) were previously included in the `elements/geometry` output as _GeometryCollection_ features. In `v2` they are instead now accessible via separate endpoints which either return the respective relation as as a whole geometry collection (e.g. `MultiPoint`, `MultiLine` or `MultiPolygon` geometry), or in form of the set of all their individual members (using the members' respective geometry and including the members' tags).
[^7]: In addition to tags and OSM metadata, the contributions extraction now also includes the tags of the respective _changesets_, which was not accessible in `v1`.

#### Request Parameters

| `v1` | `v2` | example |
| ---- | ---- | ------- |
| `bboxes` | `aoi` | <pre lang="json">"aoi": [&#13;8.68812,&#13;    49.4039,&#13;    8.72362,&#13;    49.41582&#13;]</pre> |
| `bpolys` | `aoi`[^2] | <pre lang="json">"aoi": {&#13;    "type": "Polygon",&#13;    "coordinates": […]&#13;}</pre> |
| `bcircles` | N/A | |
| `filter` | `filter` | <pre lang="json">"filter": "natural=tree"</pre> |
| `properties` | N/A (all properties are always returned) | |
| `showMetadata` | N/A | |
| `time` | `time`[^3] | <pre lang="json">"time": {&#13;    "start": "…",&#13;    "end": "…",&#13;    "interval": "…"&#13;}</pre> | |
| `timeout` | N/A | |
| `clipGeometry` | `clip`[^5] | <pre lang="json">"clip": true</pre> |

In addition to `POST` request with a JSON body for the request parameters, `GET` requests are also supported where all parameters are query paramters. In that case, the `aoi` can only be a bounding box.

#### Result Format

The extraction endpoints now return geodata in [GeoParquet](https://geoparquet.org/) format, which is compact binary format to store and distibute geo data. This can be used directly with many tools, or converted to other formats like GeoJSON for further processing.

In addition to tags and feature metadata, the 

### Metadata Endpoints

| `v1` | `v2` | comment |
| ---- | ---- | ------- |
| `/v1/metadata` | `/v2/metadata` | |
| N/A | `/v2/filter/validate` | checks whether the given filter is valid or returns a 422 Validation Error if not |
| N/A | `/v2/health` | returns wheter the API is up and running |

## Filters

The ohsome filter language remained largely the same between `v1` and `v2. Some minor differences are:

| `v1` | `v2` |
| ---- | ---- |
| `geometry:other` | `geometry:collection`[^8] |
| `squaredness` | N/A |
| `roundness` | N/A |
| `perimeter` | N/A |
| `geometry.vertices` | not yet implemented |
| N/A | `key ~ prefix*` newly available |
| N/A | `key ~ *suffix` newly available |
| N/A | `key ~ *substring*` newly available |


[^8]: see also above for how non-polygonal OSM relations are handled differently in `v2` in general


## Full Examples

<table>
<tr><td>tool/method</td><td><pre>v1</pre></td><td><pre>v2</pre></td></tr>
<tr>
<td>`curl`</td>
<td>

```sh
curl -X POST 'https://api.ohsome.org/v1/elements/count' \
  --data-urlencode 'bboxes=8.625,49.3711,8.7334,49.4397' \
  --data-urlencode 'format=csv' \
  --data-urlencode 'time=2014-01-01/2026-01-01/P1Y' \
  --data-urlencode 'filter=geometry:point and natural=tree'
```

</td>
<td>

```sh
curl -X 'POST' 'https://api.heigit.org/ohsome-api/v2/stats/features/count.csv' \
  -H 'authorization: <your-api-key>' \
  -H 'Content-Type: application/json' \
  -d '{
    "filter": "geometry:point and natural=tree",
    "aoi": [ 8.625,49.3711,8.7334,49.4397 ],
    "time": {
        "start": "2014-01-01",
        "end": "2026-01-01",
        "interval": "P1Y"
    }
}'
```

</td>
<tr>
<td>`python`</td>
<td>

```python
import https
OHSOME_API_URL = 'https://api.ohsome.org/v1/elements/count/groupBy/tag'
response = https.post(OHSOME_API_URL, data={
    "bboxes": "8.625,49.3711,8.7334,49.4397",
    "format": "csv",
    "time": "2014-01-01/2026-01-01/P1Y",
    "filter": "geometry:point and natural=tree",
    "groupByKey": "species"
})
print(response.json())
```

</td>
<td>

```python
import httpx
OHSOME_API_URL = "https://api.heigit.org/ohsome-api/v2-rc"
OHSOME_API_KEY = # insert your api key here
response = httpx.post(
    OHSOME_API_URL + "/stats/features/count.json",
    json={
        "aoi": [ 8.625,49.3711,8.7334,49.4397 ],
        "filter": "geometry:point and natural=tree",
        "time": {
            "start": "2014-01-01",
            "end": "2026-01-01",
            "interval": "P1Y",
        },
        "groupBy": {
            "type": "byTag",
            "key": "species",
        }
    },
    headers={"authorization": OHSOME_API_KEY},
)
print(response.json())
```

</td>
<tr>
<td>`ohsome-py`</td>
<td>

```python
from ohsome import OhsomeClient
client = OhsomeClient()
response = client.elements.count.post(…)
```

</td>
<td>

Discontinued. Maybe replacement will be made available at a later point in time.

</td>
</tr>

