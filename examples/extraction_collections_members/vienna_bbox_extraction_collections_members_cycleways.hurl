GET {{BASE_URL}}/extraction/collections_members.parquet
Authorization: {{API_KEY}}
accept: */*

[Query]
timestamp: latest
filter: type=route and route=bicycle
aoi: 16.149, 48.11, 16.581, 48.297
clip: false

[Options]
verbose: true
output: ./output/vienna_bbox_extraction_collections_members_cycleways.parquet

HTTP *

[Asserts]
duration < 15000
status == 200
