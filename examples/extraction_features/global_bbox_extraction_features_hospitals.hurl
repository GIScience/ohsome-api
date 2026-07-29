GET {{BASE_URL}}/extraction/features.parquet
Authorization: {{API_KEY}}
accept: */*

[Query]
timestamp: latest
filter: (amenity=hospital or healthcare=hospital) and (geometry:point or geometry:polygon)
aoi: -180,-90,180,90

[Options]
verbose: true
output: ./output/global_bbox_extraction_features_hospitals.parquet

HTTP *

[Asserts]
duration < 15000
status == 200

