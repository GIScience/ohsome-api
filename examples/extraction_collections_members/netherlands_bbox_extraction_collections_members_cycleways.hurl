GET {{BASE_URL}}/extraction/collections_members.parquet
Authorization: {{API_KEY}}
accept: */*

[Query]
timestamp: latest
filter: type=route and route=bicycle
aoi: 3.2921444,51.0671695,7.5744894,53.953531
clip: false

[Options]
verbose: true
output: ./output/netherlands_bbox_extraction_collections_members_cycleways.parquet

HTTP *

[Asserts]
duration < 90000
status == 200
