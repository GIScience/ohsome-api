# Changelog

## Current Main

### Breaking Change

* api(/stats/features): rename grouped column name (45857f1)
    - `tagValue` in CSV responses / `values` in JSON responses is now named `group` for both
* api(response/request-models): rename or remove all request/response models (f5167c3, 2b15b75, 63cba87, 88a0368)
* csv: quote all CSV fields (unix dialect of CSV) (c84aa6d)

### Bug Fixes

* time: check if start is smaller than end before generating time series and throw useful error message instead of a 500 - Internal Server Error (39fb4ac)
* config: allow config name to be split by a single underscore (7a93d85)
* actually use the dedicated extraction pool for extraction requests (33418b5)

### New Features

* Improvements to the ohsome filter:
    * New ohsome filter syntax to allow multiple geometry/osm types (`geometry:(point, line)`) (2f5356f)
    * Allow new lines in ohsome filter (dfff628)

### Other Changes

* api(/stats/features): order `groupBy` result making the it deterministic (e2de47c)
* openapi/error: add possible error status code and response models (c0be651)
* make error message more helpful by providing action suggestions (9f719cf)
* api: remove experimental state of group by parameter and add example (a2f95fe)

### Documentation

* add faq regarding api quota limits (ec3a8da)
* openapi: update example and description of csv response format (5018a72)


## 2.0.0-rc2

To migrate from v1, please [take a look the guide](https://docs.ohsome.org/ohsome-api/v2-rc/migration_guide.html).
