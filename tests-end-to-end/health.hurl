HEAD {{BASE_URL}}/health
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200

GET {{BASE_URL}}/health
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET {{BASE_URL}}/metadata
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"
