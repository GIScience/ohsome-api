HEAD {{BASE_URL}}/health
HTTP *
[Asserts]
status == 200

GET {{BASE_URL}}/health
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET {{BASE_URL}}/metadata
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"
