# /metadata /docs /openapi.json /swagger-ui-bundle /swagger-ui.css
# should be accessible without authorization

GET https://{{HOST}}/metadata
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET https://{{HOST}}/docs
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "text/html; charset=utf-8"

GET	https://{{HOST}}/openapi.json
User-Agent: HURL tests
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

# TODO: Enable once user management is integrated
# POST https://{{HOST}}/currentness/count.json
# User-Agent: HURL tests
# HTTP *
# [Asserts]
# status == 401
