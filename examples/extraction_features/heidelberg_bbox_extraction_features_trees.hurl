GET {{BASE_URL}}/extraction/features.parquet
Authorization: {{API_KEY}}
accept: */*

[Query]
timestamp: latest
filter: type:node and natural=tree
aoi: 8.68812,49.40390,8.72362,49.41582

[Options]
verbose: true
output: ./output/heidelberg_bbox_extraction_features_trees.parquet

HTTP *

[Asserts]
duration < 5000
status == 200

