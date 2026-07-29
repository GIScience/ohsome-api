GET {{BASE_URL}}/extraction/collections.parquet
Authorization: {{API_KEY}}
accept: */*

[Query]
timestamp: latest
filter: type=route and route=subway
aoi: -1.242, 44.528, 25.204, 55.318

[Options]
verbose: true
output: ./output/europe_bbox_extraction_collections_subway.parquet

HTTP *

[Asserts]
duration < 10000
status == 200

