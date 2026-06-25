# End-to-end tests

## HURL

```sh
export HURL_VARIABLE_BASE_URL=http://localhost:8000
export HURL_VARIABLE_SWAGGER_UI_CSS_URL=https://ohsome.org/static/swagger-ui.css
export HURL_VARIABLE_SWAGGER_UI_JS_URL=https://ohsome.org/static/swagger-ui-bundle.js
export HURL_SECRET_API_KEY=

hurl --test *.hurl
```

## Schemathesis

```sh
export API_KEY=foo
uvx schemathesis run \
    --phases examples \
    --exclude-checks ignored_auth \
    --tls-verify false \
    https://staging-ohsome-api.heigitk8s.de/openapi.json
```
