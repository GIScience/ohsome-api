GET {{BASE_URL}}/metadata
accept: application/json

[Options]
verbose: true

HTTP *

[Asserts]
duration < 5000
status == 200