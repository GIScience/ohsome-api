POST {{BASE_URL}}/stats/features/length.json
Authorization: {{API_KEY}}
Content-Type: application/json

[Options]
verbose: true

{
  "time": {
    "start": "2020-01-01T00:00:00Z",
    "end": "2026-04-17T00:00:00Z",
    "interval": "P1Y"
  },
  "filter": "geometry:line and highway=*",
  "aoi": [
    16.149, 48.11, 16.581, 48.297
  ],
  "groupBy": {"type": "byTag", "key": "highway"}
}

HTTP *

[Asserts]
duration < 10000
status == 200
