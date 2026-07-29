POST {{BASE_URL}}/extraction/features.parquet
Authorization: {{API_KEY}}
accept: */*

[Options]
verbose: true
output: ./output/heidelberg_geojson_extraction_features_trees.parquet

{
  "clip": true,
  "timestamp": "latest",
  "filter": "type:node and natural=tree",
  "aoi": {"type":"Polygon","coordinates":[[[8.438202,49.525424],[8.351127,49.449289],[8.440265,49.37196],[8.697463,49.336106],[8.768856,49.447309],[8.652894,49.544605],[8.438202,49.525424]]]}
}

HTTP *

[Asserts]
duration < 5000
status == 200
