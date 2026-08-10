GET https://maps.heigit.org/vector/service/ohsome/wfs
[Query]
service: wfs
request: GetFeature
typeNames: ohsome:admin_world_water
outputFormat: application/json
version: 2.0.0
srsName: EPSG:4326
CQL_FILTER: "id" IN (-285864)  # heidelberg

HTTP 200

[Captures]
geojson_coordinates: regex /"coordinates":\s*(\[[\s\S]*?\])\s*\}/
geojson_type: jsonpath "$.features[0].geometry.type"


POST {{BASE_URL}}/extraction/features.parquet
Authorization: {{API_KEY}}
Content-Type: application/json

[Options]
verbose: true
output: ./output/heidelberg_extraction_features_full_history_landuse.parquet

```json
{
  "aoi": {
    "type": "{{ geojson_type}}",
    "coordinates": {{ geojson_coordinates }}
  },
  "timestamp": {
    "start": "2010-01-01T00:00:00Z",
    "end": "2026-04-01T00:00:00Z"
  },
  "filter": "(landuse=* and landuse!=no) or natural in (wood,  grassland, scrub, heath, fell, beach, sand, scree,  shingle, bare_rock, glacier, mud, rock, cliff, fill, wetland, water, pond) or leisure in (marina, park,  garden, pitch, golf_course, playground, stadium,  recreation_ground, common, dog_park) and geometry:polygon"
}
```

HTTP *

[Asserts]
# duration < 60000
status == 200

