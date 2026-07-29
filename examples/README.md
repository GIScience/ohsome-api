# Examples

## Hurl Examples
Hurl is an HTTP command line client and HTTP test tool:

https://hurl.dev

In order to run the HTTP tests in this directory, `hurl` must be installed.

We are using hurl here mainly to write examples in a structured way.

To run all examples:
```
export HURL_VARIABLE_BASE_URL=https://api.heigit.org/ohsome-api-staging/v2
export HURL_SECRET_API_KEY=

hurl --test --file-root ./ --no-output --report-html report/  ./
```

## Add a new hurl file
To make it a bit easier to understand what each example is about, there is a proposed file name structure:

* file name pattern: `{region}_{aoi_type}_{endpoint_name}_{topic}.hurl`
* example: `europe_bbox_extraction_collections_subway.hurl`