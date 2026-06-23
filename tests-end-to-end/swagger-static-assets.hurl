# These static assets are needed for Swagger.

GET {{SWAGGER_UI_CSS_URL}}
HTTP *
[Asserts]
status == 200
header "Content-Type" == "text/css"

GET {{SWAGGER_UI_JS_URL}}
HTTP *
[Asserts]
status == 200
header "Content-Type" == "text/javascript"
