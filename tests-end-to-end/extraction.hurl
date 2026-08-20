# Authorization required
POST {{BASE_URL}}/extraction/features.parquet
Authorization: {{API_KEY}}
{
	"filter": "type:node and natural=tree",
	"time": "latest",
	"aoi": {
		"coordinates": [
			[
				[
					8.72362,
					49.41582
				],
				[
					8.68812,
					49.41582
				],
				[
					8.68812,
					49.4039
				],
				[
					8.72362,
					49.4039
				],
				[
					8.72362,
					49.41582
				]
			]
		],
		"type": "Polygon"
	}
}
HTTP *
[Asserts]
status == 200
header "Content-Length" not exists
header "Content-Type" == "application/vnd.apache.parquet"
