GET {{BASE_URL}}/extraction/features.parquet
Authorization: {{API_KEY}}
accept: */*

[Query]
timestamp: latest
filter: building=* and geometry:polygon
aoi: 16.149, 48.11, 16.581, 48.297

[Options]
verbose: true
output: ./output/vienna_bbox_extraction_features_buildings.parquet

HTTP *

[Asserts]
duration < 15000
status == 200
