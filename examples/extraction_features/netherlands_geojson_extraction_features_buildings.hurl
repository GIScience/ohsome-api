GET https://maps.heigit.org/vector/service/ohsome/wfs
[Query]
service: wfs
request: GetFeature
typeNames: ohsome:admin_world_water
outputFormat: application/json
version: 2.0.0
srsName: EPSG:4326
CQL_FILTER: "id" IN (-47796)  # netherlands

HTTP 200

[Captures]
geojson_coordinates: regex /"coordinates":\s*(\[[\s\S]*?\])\s*\}/
geojson_type: jsonpath "$.features[0].geometry.type"


POST {{BASE_URL}}/extraction/features.parquet
Authorization: {{API_KEY}}
Content-Type: application/json

[Options]
verbose: true
output: ./output/netherlands_extraction_features_buildings.parquet

```json
{
  "timestamp": "latest",
  "filter": "building=* and geometry:polygon",
  "aoi": {
    "type": "{{ geojson_type}}",
    "coordinates": {{ geojson_coordinates }}
  }
}
```

HTTP *

[Asserts]
# duration < 5000
status == 200
