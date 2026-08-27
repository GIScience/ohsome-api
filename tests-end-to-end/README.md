# End-to-end tests

## HURL

```sh
export HURL_VARIABLE_BASE_URL=http://localhost:8000
export HURL_SECRET_API_KEY=

hurl --test *.hurl
```

## Schemathesis

```sh
uvx schemathesis run \
	--rate-limit "9/m" \
    --phases examples \
    --tls-verify false \
    https://api.heigit.org/ohsome-api/v2-rc/openapi.json
```
