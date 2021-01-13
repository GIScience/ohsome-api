## current master

* update OSHDB to version 0.6.0 ([#86](https://github.com/GIScience/ohsome-api/pull/86))
  * returned values of `length`, `area` and `perimeter` requests are more precise now: The previously used formulas could be off by a few of % in some cases, now all returned values are within half a percent from the real value. ([OSHDB#193](https://github.com/GIScience/oshdb/issues/193))
  * adjusted unit tests accordingly, from now on allowing up to 0.5% discrepancy in all affected tests
  * improved performance of `groupBy/boundary` requests ([OSHDB#272](https://github.com/GIScience/oshdb/issues/272)) and processing of complex multipolygon relations ([OSHDB#249](https://github.com/GIScience/oshdb/issues/249) and [OSHDB#287](https://github.com/GIScience/oshdb/issues/287))
* data extraction: escape tags with `@` characters ([#40](https://github.com/GIScience/ohsome-api/issues/40))
* extend info on response parameters ([#73](https://github.com/GIScience/ohsome-api/pull/73))
* delombok code to enable working javadoc ([#90](https://github.com/GIScience/ohsome-api/pull/90))


## 1.2.3

### Bug Fixes

* fix a regression in version 1.2.0: `groupBy` requests return the property `groupByObjectId` instead of `groupByObject` as documented.


## 1.2.2

* provide contributions response as a downloadable file ([#78](https://github.com/GIScience/ohsome-api/pull/78))


## 1.2.1

### Bug Fixes

* fixing bug Ignite trying to load non-serializable objects within lambda functions ([#75](https://github.com/GIScience/ohsome-api/pull/75))


## 1.2.0

### Prominent Changes 

* several improvements to the docs, e.g. ([#49](https://github.com/GIScience/ohsome-api/pull/49)) and partially in ([#34](https://github.com/GIScience/ohsome-api/pull/34)) 
* upgrade to ohsome-filter 1.3 ([#62](https://github.com/GIScience/ohsome-api/pull/62))

### New Features

* adding new endpoint /contributions ([#34](https://github.com/GIScience/ohsome-api/pull/34))

### Bug Fixes

* fixing not-encoded pipe sign in GET requests ([#60](https://github.com/GIScience/ohsome-api/pull/60)) ([#61](https://github.com/GIScience/ohsome-api/pull/61))

### Other Changes

* update to oshdb version 0.5.10 (including an Ignite update to 2.9.0) ([#67](https://github.com/GIScience/ohsome-api/pull/67)) 
* diverse code refactoring improvements, e.g. ([#46](https://github.com/GIScience/ohsome-api/pull/46)), ([#47](https://github.com/GIScience/ohsome-api/pull/47)), ([#51](https://github.com/GIScience/ohsome-api/pull/51)), ([#57](https://github.com/GIScience/ohsome-api/pull/57))
* include test directory in checkstyle ([#69](https://github.com/GIScience/ohsome-api/pull/69))


## 1.1.1

### Bug Fixes

* allowing not encoded pipe sign in GET requests ([#60](https://github.com/GIScience/ohsome-api/pull/60))


## 1.1.0

### Prominent Changes 

* making filter2 parameter mandatory for /ratio requests and changing filter to be allowed to be omitted or empty ([#27](https://github.com/GIScience/ohsome-api/pull/27))
* upgrading to ohsome filter 1.2 ([#38](https://github.com/GIScience/ohsome-api/pull/38)) to be able to filter on OSM feature IDs, on a list of tags with the same key and support of empty filters
* update of readme docs to give little intro and more usability examples ([#15](https://github.com/GIScience/ohsome-api/pull/15))

### New Features

* addition of parameter ‘clipGeometry’ for data-extraction endpoints ([#29](https://github.com/GIScience/ohsome-api/pull/29))
* restricting each parameter to be unique in the request ([#12](https://github.com/GIScience/ohsome-api/pull/12))

### Bug Fixes

* adapting /groupBy/tag csv response to give empty result if no data matches given filters ([#33](https://github.com/GIScience/ohsome-api/pull/33))
* give exception when no parameters are given ([#13](https://github.com/GIScience/ohsome-api/pull/13))

### Other Changes

* adding badges to the readme ([#39](https://github.com/GIScience/ohsome-api/pull/39))
* marking types, keys and values parameter usage as deprecated and adapting swagger docs to use filter ([#14](https://github.com/GIScience/ohsome-api/pull/14)) 
* update ohsome parent to version 2.4 (Java 11 compatibility) ([#24](https://github.com/GIScience/ohsome-api/pull/24))
* refactoring of diverse code parts, e.g. zero-fill usage ([a8856c9](https://github.com/GIScience/ohsome-api/commit/a8856c90c53a10410eaeeae91f5c31173f1e49d6)), handling of clipping the geometries ([3628d04](https://github.com/GIScience/ohsome-api/pull/29/commits/3628d042df46634d31e8f152354689ef1f1d5b08)) 


## 1.0.0

### Prominent Changes

* Implement new powerful filter parameter
* Release source code under AGPL license
* Adding of sphinx-docs under /docs folder (55a31914)
* Removal of userids parameter and /groupBy/user resource
* Removal of /share resource (452ea663)
* New category 'total' in response for /users/count/groupBY/tag|key (41d0c830)
* Deactivating contribution types in full-history response (1d7a1ec7)

### New Features

* Integration of csv as output format (3850891d)
* Implementing cache logic used via HTTP headers
* Parameter timeout to define custom timeout for request (8ebec3ba)
* Nested group-by resource: /groupBy/boundary/groupBy/tag (cb82b9f6)
* Implementing simple feature types support (fba76544, c2dcc837)

### Bug Fixes

* Wrong response for GeoJSON boundaries lying outside of data extract
* Request parameter usage
* Content type header and encoding of csv response
* Proper request encoding if encoding is not defined
* Wrong response when same geometry is used for different input features
* Not fully working unclipped property for data extraction
* Several other bugs (04a798b5, 01eca91f, 585933c7, 16aec329)

### Performance and Code Quality

* Faster union computation (4744e843)
* Caching of time-independent results (aae6591d) 
* Rework of time parameter processing (a000b598)
* Refactoring of several code parts (e.g.: 5a8fba0c, 42064696, 67ef11fb, 5230de69)
* Adding of csv output tests (7982338a)
* Adding of Ignite cluster nodes check on runtime (bc8e6fdf)
* Improving data extraction processing (3401db9d, 4f438fd5, f19e5b2b)
* Optimize tag translation in data extraction requests (0683ed83)
* Applying diverse checkstyle, pmd, etc. suggestions (9de5240a, 171ebd5d)

### Other Changes

* OSHDB version 0.5.8
* Adding this Changelog
* Start spring-boot application after connection with oshdb has been fully established (25ee2e15)


## 0.9.7

### Prominent Changes

* Springfox to 2.9.2, which results in new Swagger-UI version (93b4e55d)
* OSHDB to 0.5.0-SNAPSHOT (b5ff6195)
* Gzip compression for GeoJSON response (83c43fca)

### New Features

* Adding GeoJSON output format to all /groupBy/boundary resources (5bd6f4c2, 5a416395)
* Parameter 'format' (1aaba309, 97e984e7)
* New resources e.g. /density/groupBy/boundary (541ebe01, 86756acc)
* OSM data-extraction via /elements and /elementsFullHistory (e.g. 57f3540f, dd7d2a9e, f40f91d8)
     * Streams the response on Ignite, if it is bigger than 10MB (d7107bcc)
* New startup-parameters
     * For controlling the test-execution (e.g. -Dport_data='port' to execute data-extraction integration tests)
     * --port to define the port for the API
     * --database.timeout to define the maximum processing time of a request in ms (cc3061a3)

### Bug Fixes

* Fixing of diverse bugs (4eab57c0, 4d3bf2ed)

### Performance and Code Quality

* Implementing >=1 integration test for each resource (e.g. 9ca59338, 8e170aa9, 48d5f176)
* Renaming/combining of several classes and methods to better fit their purpose (ab5295ca)
* Massive reduction of code due to combination of processing of GET and POST requests (9aa0511b)
* Using CheckStyle for checking the code compliance with the Google Java styling and applying it (8d2e5685)
* Better exception handling and response messages
* Removing of unused parameters and code parts
* Adapting Javadoc (e.g. 121371b1)


## 0.9.6

### Prominent Changes

* showMetadata parameter can now accept true/false as input (469b0316)

### New Features

* /users/count/groupBy/key (e71b9902)

### Bugfixes

* Fixing wrong results for specific keys/values combinations using /share (092b3c1c)
* Fixing too high results in some /users resources (ec5626d1)

### Performance and Code Quality

* Faster /ratio computation (0a991edb, 31ecae11)
* Removed duplicated code (e.g.: e6958eb8, 87a84d4b, 3a815731)
* Including of /share computation in /ratio processing (6164ef85)
* Improving/fixing Javadoc comments and paths

### Other Changes

* ASCII representation of the HeiGIT logo in the console when running the jar file (77295e1f)
* showMetadata and types input can be written in upper and lower case (469b0316)
* Request URL in error response will only be included for GET requests (fa0bf285)
* Spaces, empty lines, etc. are neglected in processing of bpolys and showMetadata input (f559b9be)
