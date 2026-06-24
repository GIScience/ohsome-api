# Authorization required
POST {{BASE_URL}}/features/extraction.parquet
Authorization: {{API_KEY}}
{
  "filter": "type:node and natural=tree",
  "aoi": {
    "features": [
      {
        "geometry": {
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
        },
        "properties": {},
        "type": "Feature"
      }
    ],
    "type": "FeatureCollection"
  }
}
HTTP *
[Asserts]
status == 200
header "Transfer-Encoding" == "chunked"
header "Content-Type" == "application/vnd.apache.parquet"
