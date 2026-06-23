# End-to-end tests with HURL

```sh
export HURL_VARIABLE_BASE_URL=http://localhost:8000
export HURL_VARIABLE_SWAGGER_UI_CSS=https://ohsome.org/static/swagger-ui.css
export HURL_VARIABLE_SWAGGER_UI_JS=https://ohsome.org/static/swagger-ui-bundle.js
export HURL_SECRET_API_KEY=
hurl --test *.hurl
```
