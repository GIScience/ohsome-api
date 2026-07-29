POST {{BASE_URL}}/stats/currentness/count.json
Authorization: {{API_KEY}}
Content-Type: application/json

[Options]
verbose: true

{
  "timeBins": {
    "start": "2026-01-01T00:00:00Z",
    "end": "2026-04-17T00:00:00Z",
    "binSize": "P1M"
  },
  "filter": "building=* and geometry:polygon",
  "aoi": [
    8.68812,
    49.4039,
    8.72362,
    49.41582
  ]
}

HTTP *

[Asserts]
duration < 5000
status == 200

