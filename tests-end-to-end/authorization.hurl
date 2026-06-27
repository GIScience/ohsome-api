# Authorization required
POST {{BASE_URL}}/currentness/count.json
Authorization: {{API_KEY}}
{
	"filter": "type:node and natural=tree",
	"aoi": {
		"coordinates": [
			[
				[8.72362, 49.41582],
				[8.68812, 49.41582],
				[8.68812, 49.4039],
				[8.72362, 49.4039],
				[8.72362, 49.41582]
			]
		],
		"type": "Polygon"
	},
	"timeBins": {
		"start": "2026-01-01T00:00:00Z",
		"end": "2026-04-17T00:00:00Z",
		"binSize": "P1M"
	}
}
HTTP *
[Asserts]
status == 200
