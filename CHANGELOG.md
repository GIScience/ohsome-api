## 0.9.8.SNAPSHOT (current master)



## 0.9.7

### Prominent Changes

* Springfox to 2.9.2 (results in new Swagger-UI version)
* OSHDB to 0.5.0-SNAPSHOT

### New Features

* Adding GeoJSON output format to all /groupBy/boundary resources
* New resources e.g. /density/groupBy/boundary
* OSM data-extraction via /elements and /elementsFullHistory
     * Streams the response on Ignite, if it is bigger than 10MB
* New startup-parameters
     * For controlling the test-execution (e.g. -Dport_data='port' to execute data-extraction integration tests)
     * --port to define the port for the API
     * --database.timeout to define the maximum processing time of a request in ms

### Bug Fixes

* Fixing of diverse bugs

### Performance and Code Quality

* Implementing >=1 integration test for each resource
* Whole API now has 99 tests
* Renaming/combining of several classes and methods to better fit their purpose
* Massive reduction of code due to combination of processing of GET and POST requests
* Using CheckStyle for checking the code compliance with the Google Java styling and applying it
* Better exception handling and response messages
* Removing of unused parameters and code parts

### Other Changes


## 0.9.6

### Prominent Changes

* showMetadata parameter can now accept true/false as input

### New Features

* /users/count/groupBy/key

### Bugfixes

* Fixing wrong results for specific keys/values combinations using /share
* Fixing too high results in some /users resources (all but /count)

### Performance and Code Quality

* Faster /ratio computation
* Removed duplicated code
* Including of /share computation in /ratio processing
* Improving/fixing Javadoc comments and paths

### Other Changes

* ASCII representation of the HeiGIT logo in the console when running the jar file
* showMetadata and types input can be written in upper and lower case
* Request URL in error response will only be included for GET requests
* Spaces, empty lines, etc. are neglected in processing of bpolys and showMetadata input
