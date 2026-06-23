HEAD https://{{HOST}}/health
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200

GET https://{{HOST}}/health
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET https://{{HOST}}/metadata
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"
