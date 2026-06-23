HEAD {{BASE_URL}}/health
User-Agent: HeiGIT ohsome API end-to-end tests
HTTP *
[Asserts]
status == 200

GET {{BASE_URL}}/health
User-Agent: HeiGIT ohsome API end-to-end tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET {{BASE_URL}}/metadata
User-Agent: HeiGIT ohsome API end-to-end tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"
