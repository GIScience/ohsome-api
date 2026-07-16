# These endpoints should be accessible without authorization.

GET {{BASE_URL}}/metadata
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET {{BASE_URL}}/docs
HTTP *
[Asserts]
status == 200
header "Content-Type" == "text/html; charset=utf-8"

GET	{{BASE_URL}}/openapi.json
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

GET	{{BASE_URL}}/filter/validation?filter=tree=yes
HTTP *
[Asserts]
status == 200
header "Content-Type" == "application/json"

# Authorization required.
POST {{BASE_URL}}/stats/currentness/count.json
HTTP *
[Asserts]
status == 401
