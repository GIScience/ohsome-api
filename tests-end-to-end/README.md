# End-to-end tests with HURL

```sh
export HURL_VARIABLE_BASE_URL=http://localhost:8000
export HURL_VARIABLE_SWAGGER_UI_CSS_URL=https://ohsome.org/static/swagger-ui.css
export HURL_VARIABLE_SWAGGER_UI_JS_URL=https://ohsome.org/static/swagger-ui-bundle.js
export HURL_SECRET_API_KEY=
hurl --test *.hurl
```
