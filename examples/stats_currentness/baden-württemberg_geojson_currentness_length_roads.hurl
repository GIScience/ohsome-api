GET https://maps.heigit.org/vector/service/ohsome/wfs
[Query]
service: wfs
request: GetFeature
typeNames: ohsome:admin_world_water
outputFormat: application/json
version: 2.0.0
srsName: EPSG:4326
CQL_FILTER: "id" IN (-62611)

HTTP 200

[Captures]
geojson_coordinates: regex /"coordinates":\s*(\[[\s\S]*?\])\s*\}/
geojson_type: jsonpath "$.features[0].geometry.type"


POST {{BASE_URL}}/stats/currentness/length.csv
Authorization: {{API_KEY}}
Content-Type: application/json

[Options]
verbose: true

```json
{
  "timeBins": {
    "start": "2010-01-01T00:00:00Z",
    "end": "latest",
    "binSize": "P1M"
  },
  "filter": "highway in (motorway, trunk, primary, secondary, tertiary, residential, service, living_street, trunk_link, motorway_link, primary_link, secondary_link, tertiary_link, unclassified) and geometry:line",
  "aoi": {
    "type": "{{ geojson_type}}",
    "coordinates": {{ geojson_coordinates }}
  }
}
```

HTTP *

[Asserts]
duration < 5000
status == 200
