# /metadata /docs /openapi.json /swagger-ui-bundle /swagger-ui.css
# should be accessible without authorization

GET {{BASE_URL}}/metadata
User-Agent: HeiGIT ohsome API end-to-end tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET {{BASE_URL}}/docs
User-Agent: HeiGIT ohsome API end-to-end tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "text/html; charset=utf-8"

GET	{{BASE_URL}}/openapi.json
User-Agent: HeiGIT ohsome API end-to-end tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

# TODO: Enable once user management is integrated
# POST {{BASE_URL}}/currentness/count.json
# User-Agent: HURL tests
# HTTP *
# [Asserts]
# status == 401
