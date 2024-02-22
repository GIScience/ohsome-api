Changelog
=========

## 1.11.0-SNAPSHOT (current main)


## 1.10.2

### Bug Fixes
* Fix spaces from being removed from parameters like `groupByValues` ([#305])
* Fix inconsistent timestamp format in `/elements/(aggregation)/ratio/groupBy/boundary` request responses ([#318])
### Other
* Upgrade OSHDB to version 1.10.2

[#313]: https://github.com/GIScience/ohsome-api/issues/313
[#318]: https://github.com/GIScience/ohsome-api/issues/318


## 1.10.1

* Fix performance degradation in previous release (version 1.10.0) which made data extractions very slow for medium to large query areas ([oshdb#516])

[oshdb#516]: https://github.com/GIScience/oshdb/pull/516


## 1.10.0

### Performance Improvements
* Upgrade OSHDB to version 1.2.0, significantly speeding up queries which touch many entities, i.e. frequent tags like `building` ([oshdb#511])

### Bug Fixes
* Fix bug which prevented some filters (which only work on ContributionView-based queries) to correctly work in contribution extraction endpoints ([#305])

### Other
* Add additional query metrics to log output: the used `filter`, `time` and some stats about the boundary parameter (type, bounding box and area) ([#304])
* Update ohsome parent module, switch to new HeiGIT maven repository ([#307])

[#304]: https://github.com/GIScience/ohsome-api/pull/304
[#305]: https://github.com/GIScience/ohsome-api/issues/305
[#307]: https://github.com/GIScience/ohsome-api/pull/307
[oshdb#511]: https://github.com/GIScience/oshdb/pull/511


## 1.9.1

* Upgrade OSHDB to version 1.1.2 ([#302])

[#302]: https://github.com/GIScience/ohsome-api/issues/302


## 1.9.0

### Bug Fixes
* Show proper error message when a `changeset` filter is used on a snapshot based endpoint ([#289])

### Other Changes
* Upgrade OSHDB to version 1.1.1
* This version requires Java 17 to run

[#289]: https://github.com/GIScience/ohsome-api/issues/289


## 1.8.1

### Bug Fixes
* Fix crash and incorrect output of `…/groupBy/tag` and `…/groupBy/key` endpoints when non-existing tag keys or values are used in a query ([#291])

### Documentation
* Complete documentation of ohsome filters ([#290])

[#290]: https://github.com/GIScience/ohsome-api/pull/290
[#291]: https://github.com/GIScience/ohsome-api/pull/291


## 1.8.0

### Breaking Changes
* remove caching command line parameter ([#281])
* update to OSHDB 1.0.0 ([#281], [#283])

### New Features
* tag translator parameter are now configurable via CLI parameter: `tt.maxbytesvalue`, `tt.maxnumroles` ([#281])

[#281]: https://github.com/GIScience/ohsome-api/pull/281
[#283]: https://github.com/GIScience/ohsome-api/pull/283


## 1.7.0

### Breaking Changes
* update OSHDB to version 1.0.0-beta-1 ([#278])
  * OSM type strings change to `node`, `way`, `relation`

### New Features

* add `contributions/count[/density]/groupBy/boundary` endpoints to fetch contribution counts/densities grouped by boundary ([#217])
* add an error message for requests without defined parameters ([#227])
* add `contributionType` parameter to all `/users` endpoints ([#224])

### Bug Fixes

* fix bug which prevented error messages from being returned for non-unique parameters in POST requests ([#228])
* fix a regression causing `…/groupBy/boundary` to crash when used with a geometry type filter using the (deprecated) types/keys/values parameter syntax ([#230])
* fix changed name of property setting HTTP POST size ([#274])

[#217]: https://github.com/GIScience/ohsome-api/issues/217
[#224]: https://github.com/GIScience/ohsome-api/issues/224
[#227]: https://github.com/GIScience/ohsome-api/issues/227
[#228]: https://github.com/GIScience/ohsome-api/pull/228
[#230]: https://github.com/GIScience/ohsome-api/pull/230
[#274]: https://github.com/GIScience/ohsome-api/pull/274
[#278]: https://github.com/GIScience/ohsome-api/pull/278


## 1.6.3

* upgrade `spring-boot` dependency to version 2.5.12 (and `spring` to version 5.3.18) ([#272])

[#272]: https://github.com/GIScience/ohsome-api/pull/272


## 1.6.2

* remove unused `log4j-api` dependency ([#251])

[#251]: https://github.com/GIScience/ohsome-api/pull/251


## 1.6.1

* upgrade OSHDB to version [`0.7.2`](https://github.com/GIScience/oshdb/releases/tag/0.7.2), with bugfixes for not working `length:` and `area:` filters on ignite and improved job canceling ([#237])

[#237]: https://github.com/GIScience/ohsome-api/pull/237


## 1.6.0

### Bug Fixes

* fix wrong thrown exceptions in case of invalid `bpolys` boundary ([#214])
* fix not thrown exception in case of `bpolys` and `bcircles` boundaries not lying completely within the underlying data-extract polygon ([#201])

### Performance Improvements

* avoid unnecessary geometry building for `count/ratio` requests ([#161])

### Other Changes

* upgrade to OSHDB version 0.7.1 – note that OSHDB database files from the previous version are not compatible with the OSHDB version 0.7 anymore, so you have to either [redownload](https://downloads.ohsome.org/OSHDB/v0.7/) or [recreate](https://github.com/GIScience/oshdb/blob/0.7/oshdb-etl/README.md) them from scratch ([#222], [#225])
* adapt error message for contributions extraction endpoint in case of wrong `time` parameter value ([#208])

[#161]: https://github.com/GIScience/ohsome-api/pull/161
[#201]: https://github.com/GIScience/ohsome-api/issues/201
[#208]: https://github.com/GIScience/ohsome-api/issues/208
[#214]: https://github.com/GIScience/ohsome-api/issues/214
[#222]: https://github.com/GIScience/ohsome-api/pull/222
[#225]: https://github.com/GIScience/ohsome-api/pull/225


## 1.5.0

### New Features

* add new `contributionTypes` property to `properties` parameter ([#136])
* add new `/contributions/latest/count` and `/contributions/latest/count/density` endpoints ([#174])
* add new `contributionType` parameter for contributions aggregation ([#174])
* contributions extraction endpoints now return the `@contributionChangesetId` as a separate field ([#200])

### Bug Fixes

* fix uncaught GeoJSON parsing exception ([#55])
* fix a bug where `getMetadataTest` unit test fails in certain setups ([#175])
* remove the parameters `@snapshotTimestamp` and `@lastEdit` from full-history extraction responses ([#191])

### Other Changes

* add information regarding a potential "NaN" value for `ratio` results to docs ([#166])
* properly skip `GetControllerTest` class if `port1` parameter is absent ([#164])
* update ohsome parent module, requires maven version 3.6 or higher ([#184])
* update documentation([#88]) ([#150]) and endpoints graph ([#158])
* remove deprecated request parameters from swagger definition and UI ([#168])

[#55]: https://github.com/GIScience/ohsome-api/issues/55
[#88]: https://github.com/GIScience/ohsome-api/issues/88
[#136]: https://github.com/GIScience/ohsome-api/issues/136
[#150]: https://github.com/GIScience/ohsome-api/issues/150
[#158]: https://github.com/GIScience/ohsome-api/issues/158
[#164]: https://github.com/GIScience/ohsome-api/pull/164
[#166]: https://github.com/GIScience/ohsome-api/issues/166
[#168]: https://github.com/GIScience/ohsome-api/pull/168
[#174]: https://github.com/GIScience/ohsome-api/issues/174
[#175]: https://github.com/GIScience/ohsome-api/issues/175
[#184]: https://github.com/GIScience/ohsome-api/pull/184
[#191]: https://github.com/GIScience/ohsome-api/issues/191


## 1.4.2

* contributions extraction endpoints now return the `@contributionChangesetId` as a separate field (backported from 1.5.0) ([#200])

[#200]: https://github.com/GIScience/ohsome-api/issues/200


## 1.4.1

* upgrade OSHDB to version [OSHDB#0.6.4], fixes the following two bugs:
* fix crash when a non-existent tag is used in a filter ([#154])
* fix crash in `groupBy/boundary` queries caused by invalid OSM geometries ([OSHDB#362])

[OSHDB#0.6.4]: https://github.com/GIScience/oshdb/releases/tag/0.6.4
[OSHDB#362]: https://github.com/GIScience/oshdb/pull/362
[#154]: https://github.com/GIScience/ohsome-api/issues/154


## 1.4.0

### New Features

* add new endpoint /contributions/count/{density} ([#115])

### Bug Fixes

* fix some invalid filters in the default swagger examples ([#111])
* fix returning invalid GeoJSON using empty coordinates for deletion contributions ([#129], [#131])
* fix using a proper boolean data type instead of a string for contributionType in response ([#135])
* fix a null pointer exception in data extraction requests ([#141], [#145])
* make sure geometry filters are applied to all returned features of elementsFullHistory requests ([#109])

### Performance and Code Quality

* improve performance of ratio requests ([#114])

### Other Changes

* update all tests using the filter parameter instead of deprecated types, keys, values ([#98])
* update some default parameter values in swagger UI to slightly more sensible examples ([#113])
* restructure packages and classes within the controller and output packages ([#117])
* extend docs on contribution types ([#134])
* round coordinates of returned OSM features to 7 decimal places ([#138])
* improve code style by wrapping all code lines longer than 100 characters ([#83])
* update ohsome parent to version 2.8 ([#152])

[#83]: https://github.com/GIScience/ohsome-api/issues/83
[#98]: https://github.com/GIScience/ohsome-api/issues/98
[#109]: https://github.com/GIScience/ohsome-api/issues/109
[#111]: https://github.com/GIScience/ohsome-api/issues/111
[#113]: https://github.com/GIScience/ohsome-api/issues/113
[#114]: https://github.com/GIScience/ohsome-api/pull/114
[#115]: https://github.com/GIScience/ohsome-api/issues/115
[#117]: https://github.com/GIScience/ohsome-api/issues/117
[#129]: https://github.com/GIScience/ohsome-api/issues/129
[#131]: https://github.com/GIScience/ohsome-api/issues/131
[#134]: https://github.com/GIScience/ohsome-api/issues/134
[#135]: https://github.com/GIScience/ohsome-api/pull/135
[#138]: https://github.com/GIScience/ohsome-api/issues/138
[#141]: https://github.com/GIScience/ohsome-api/issues/141
[#145]: https://github.com/GIScience/ohsome-api/pull/145
[#152]: https://github.com/GIScience/ohsome-api/pull/152


## 1.3.2

### Bug Fixes

* update OSHDB to 0.6.3 to fix a bug where certain invalid multipolygons cause an infinite loop ([OSHDB#343])

[OSHDB#343]: https://github.com/GIScience/oshdb/pull/343


## 1.3.1

### Bug Fixes

* fix crash when `elementsFullHistory` requests use the `filter` parameter ([#97])
* fix a bug where `groupBy/boundary` requests do ignore a given `filter` ([#99])
* update to OSHDB version 0.6.2, fixing a crash when building certain invalid multipolygon relations ([OSHDB#334])

[#97]: https://github.com/GIScience/ohsome-api/pull/97
[#99]: https://github.com/GIScience/ohsome-api/issues/99
[OSHDB#334]: https://github.com/GIScience/oshdb/issues/334


## 1.3.0

### Prominent Changes

* update OSHDB to version 0.6.1 ([#86])
  * returned values of `length`, `area` and `perimeter` requests are more precise now: The previously used formulas could be off by a few of % in some cases, now all returned values are within half a percent from the real value. ([OSHDB#193])
  * adjusted unit tests accordingly, from now on allowing up to 0.5% discrepancy in all affected tests
  * improved performance of `groupBy/boundary` requests ([OSHDB#272]) and processing of complex multipolygon relations ([OSHDB#249] and [OSHDB#287])

### Bug Fixes

* delombok code to enable working javadoc ([#90])

### Other Changes

* data extraction: escape tags with `@` characters ([#40])
* extend info on response parameters ([#73])

[#40]: https://github.com/GIScience/ohsome-api/issues/40
[#73]: https://github.com/GIScience/ohsome-api/pull/73
[#86]: https://github.com/GIScience/ohsome-api/pull/86
[#90]: https://github.com/GIScience/ohsome-api/pull/90
[OSHDB#193]: https://github.com/GIScience/oshdb/issues/193
[OSHDB#249]: https://github.com/GIScience/oshdb/issues/249
[OSHDB#272]: https://github.com/GIScience/oshdb/issues/272
[OSHDB#287]: https://github.com/GIScience/oshdb/issues/287


## 1.2.3

### Bug Fixes

* fix a regression in version 1.2.0: `groupBy` requests return the property `groupByObjectId` instead of `groupByObject` as documented.


## 1.2.2

### Bug Fixes

* provide contributions response as a downloadable file ([#78])

[#78]: https://github.com/GIScience/ohsome-api/pull/78


## 1.2.1

### Bug Fixes

* fixing bug Ignite trying to load non-serializable objects within lambda functions ([#75])

[#75]: https://github.com/GIScience/ohsome-api/pull/75


## 1.2.0

### Prominent Changes

* several improvements to the docs (e.g. [#49] and partially in [#34])
* upgrade to ohsome-filter 1.3 ([#62])

### New Features

* adding new endpoint /contributions ([#34])

### Bug Fixes

* fixing not-encoded pipe sign in GET requests ([#60], [#61])

### Other Changes

* update to oshdb version 0.5.10 (including an Ignite update to 2.9.0) ([#67])
* diverse code refactoring improvements (e.g. [#46], [#47], [#51], [#57])
* include test directory in checkstyle ([#69])

[#34]: https://github.com/GIScience/ohsome-api/pull/34
[#46]: https://github.com/GIScience/ohsome-api/pull/46
[#47]: https://github.com/GIScience/ohsome-api/pull/47
[#49]: https://github.com/GIScience/ohsome-api/pull/49
[#51]: https://github.com/GIScience/ohsome-api/pull/51
[#57]: https://github.com/GIScience/ohsome-api/pull/57
[#60]: https://github.com/GIScience/ohsome-api/pull/60
[#61]: https://github.com/GIScience/ohsome-api/pull/61
[#62]: https://github.com/GIScience/ohsome-api/pull/62
[#67]: https://github.com/GIScience/ohsome-api/pull/67
[#69]: https://github.com/GIScience/ohsome-api/pull/69


## 1.1.1

### Bug Fixes

* allowing not encoded pipe sign in GET requests ([#60])

[#60]: https://github.com/GIScience/ohsome-api/pull/60


## 1.1.0

### Prominent Changes 

* making filter2 parameter mandatory for /ratio requests and changing filter to be allowed to be omitted or empty ([#27])
* upgrading to ohsome filter 1.2 ([#38]) to be able to filter on OSM feature IDs, on a list of tags with the same key and support of empty filters
* update of readme docs to give little intro and more usability examples ([#15])

### New Features

* addition of parameter ‘clipGeometry’ for data-extraction endpoints ([#29])
* restricting each parameter to be unique in the request ([#12])

### Bug Fixes

* adapting /groupBy/tag csv response to give empty result if no data matches given filters ([#33])
* give exception when no parameters are given ([#13])

### Other Changes

* adding badges to the readme ([#39])
* marking types, keys and values parameter usage as deprecated and adapting swagger docs to use filter ([#14])
* update ohsome parent to version 2.4 (Java 11 compatibility) ([#24])
* refactoring of diverse code parts, e.g. zero-fill usage ([a8856c9]), handling of clipping the geometries ([3628d04])

[#12]: https://github.com/GIScience/ohsome-api/pull/12
[#13]: https://github.com/GIScience/ohsome-api/pull/13
[#14]: https://github.com/GIScience/ohsome-api/pull/14
[#15]: https://github.com/GIScience/ohsome-api/pull/15
[#24]: https://github.com/GIScience/ohsome-api/pull/24
[#27]: https://github.com/GIScience/ohsome-api/pull/27
[#29]: https://github.com/GIScience/ohsome-api/pull/29
[#33]: https://github.com/GIScience/ohsome-api/pull/33
[#38]: https://github.com/GIScience/ohsome-api/pull/38
[#39]: https://github.com/GIScience/ohsome-api/pull/39
[a8856c9]: https://github.com/GIScience/ohsome-api/commit/a8856c90c53a10410eaeeae91f5c31173f1e49d6
[3628d04]: https://github.com/GIScience/ohsome-api/pull/29/commits/3628d042df46634d31e8f152354689ef1f1d5b08


## 1.0.0

### Prominent Changes

* Implement new powerful filter parameter
* Release source code under AGPL license
* Adding of sphinx-docs under /docs folder ([55a31914])
* Removal of userids parameter and /groupBy/user resource
* Removal of /share resource ([452ea663])
* New category 'total' in response for /users/count/groupBY/tag|key ([41d0c830])
* Deactivating contribution types in full-history response ([1d7a1ec7])

### New Features

* Integration of csv as output format ([3850891d])
* Implementing cache logic used via HTTP headers
* Parameter timeout to define custom timeout for request ([8ebec3ba])
* Nested group-by resource: /groupBy/boundary/groupBy/tag ([cb82b9f6])
* Implementing simple feature types support ([fba76544], [c2dcc837])

### Bug Fixes

* Wrong response for GeoJSON boundaries lying outside of data extract
* Request parameter usage
* Content type header and encoding of csv response
* Proper request encoding if encoding is not defined
* Wrong response when same geometry is used for different input features
* Not fully working unclipped property for data extraction
* Several other bugs ([04a798b5], [01eca91f], [585933c7], [16aec329])

### Performance and Code Quality

* Faster union computation ([4744e843])
* Caching of time-independent results ([aae6591d])
* Rework of time parameter processing ([a000b598])
* Refactoring of several code parts (e.g. [5a8fba0c], [42064696], [67ef11fb], [5230de69])
* Adding of csv output tests ([7982338a])
* Adding of Ignite cluster nodes check on runtime ([bc8e6fdf])
* Improving data extraction processing ([3401db9d], [4f438fd5], [f19e5b2b])
* Optimize tag translation in data extraction requests ([0683ed83])
* Applying diverse checkstyle, pmd, etc. suggestions ([9de5240a], [171ebd5d])

### Other Changes

* OSHDB version 0.5.8
* Adding this Changelog
* Start spring-boot application after connection with oshdb has been fully established ([25ee2e15])

[01eca91f]: https://github.com/GIScience/ohsome-api/commit/01eca91f
[04a798b5]: https://github.com/GIScience/ohsome-api/commit/04a798b5
[0683ed83]: https://github.com/GIScience/ohsome-api/commit/0683ed83
[16aec329]: https://github.com/GIScience/ohsome-api/commit/16aec329
[171ebd5d]: https://github.com/GIScience/ohsome-api/commit/171ebd5d
[1d7a1ec7]: https://github.com/GIScience/ohsome-api/commit/1d7a1ec7
[25ee2e15]: https://github.com/GIScience/ohsome-api/commit/25ee2e15
[3401db9d]: https://github.com/GIScience/ohsome-api/commit/3401db9d
[3850891d]: https://github.com/GIScience/ohsome-api/commit/3850891d
[41d0c830]: https://github.com/GIScience/ohsome-api/commit/41d0c830
[42064696]: https://github.com/GIScience/ohsome-api/commit/42064696
[452ea663]: https://github.com/GIScience/ohsome-api/commit/452ea663
[4744e843]: https://github.com/GIScience/ohsome-api/commit/4744e843
[4f438fd5]: https://github.com/GIScience/ohsome-api/commit/4f438fd5
[5230de69]: https://github.com/GIScience/ohsome-api/commit/5230de69
[55a31914]: https://github.com/GIScience/ohsome-api/commit/55a31914
[585933c7]: https://github.com/GIScience/ohsome-api/commit/585933c7
[5a8fba0c]: https://github.com/GIScience/ohsome-api/commit/5a8fba0c
[67ef11fb]: https://github.com/GIScience/ohsome-api/commit/67ef11fb
[7982338a]: https://github.com/GIScience/ohsome-api/commit/7982338a
[8ebec3ba]: https://github.com/GIScience/ohsome-api/commit/8ebec3ba
[9de5240a]: https://github.com/GIScience/ohsome-api/commit/9de5240a
[a000b598]: https://github.com/GIScience/ohsome-api/commit/a000b598
[aae6591d]: https://github.com/GIScience/ohsome-api/commit/aae6591d
[bc8e6fdf]: https://github.com/GIScience/ohsome-api/commit/bc8e6fdf
[c2dcc837]: https://github.com/GIScience/ohsome-api/commit/c2dcc837
[cb82b9f6]: https://github.com/GIScience/ohsome-api/commit/cb82b9f6
[f19e5b2b]: https://github.com/GIScience/ohsome-api/commit/f19e5b2b
[fba76544]: https://github.com/GIScience/ohsome-api/commit/fba76544


## 0.9.7

### Prominent Changes

* Springfox to 2.9.2, which results in new Swagger-UI version ([93b4e55d])
* OSHDB to 0.5.0-SNAPSHOT ([b5ff6195])
* Gzip compression for GeoJSON response ([83c43fca])

### New Features

* Adding GeoJSON output format to all /groupBy/boundary resources ([5bd6f4c2], [5a416395])
* Parameter 'format' ([1aaba309], [97e984e7])
* New resources, e.g. /density/groupBy/boundary ([541ebe01], [86756acc])
* OSM data-extraction via /elements and /elementsFullHistory (e.g. [57f3540f], [dd7d2a9e], [f40f91d8])
     * Streams the response on Ignite, if it is bigger than 10MB ([d7107bcc])
* New startup-parameters
     * For controlling the test-execution (e.g. -Dport_data='port' to execute data-extraction integration tests)
     * --port to define the port for the API
     * --database.timeout to define the maximum processing time of a request in ms ([cc3061a3])

### Bug Fixes

* Fixing of diverse bugs ([4eab57c0], [4d3bf2ed])

### Performance and Code Quality

* Implementing >=1 integration test for each resource (e.g. [9ca59338], [8e170aa9], [48d5f176])
* Renaming/combining of several classes and methods to better fit their purpose ([ab5295ca])
* Massive reduction of code due to combination of processing of GET and POST requests ([9aa0511b])
* Using CheckStyle for checking the code compliance with the Google Java styling and applying it ([8d2e5685])
* Better exception handling and response messages
* Removing of unused parameters and code parts
* Adapting Javadoc (e.g. [121371b1])

[121371b1]: https://github.com/GIScience/ohsome-api/commit/121371b1
[1aaba309]: https://github.com/GIScience/ohsome-api/commit/1aaba309
[48d5f176]: https://github.com/GIScience/ohsome-api/commit/48d5f176
[4d3bf2ed]: https://github.com/GIScience/ohsome-api/commit/4d3bf2ed
[4eab57c0]: https://github.com/GIScience/ohsome-api/commit/4eab57c0
[541ebe01]: https://github.com/GIScience/ohsome-api/commit/541ebe01
[57f3540f]: https://github.com/GIScience/ohsome-api/commit/57f3540f
[5a416395]: https://github.com/GIScience/ohsome-api/commit/5a416395
[5bd6f4c2]: https://github.com/GIScience/ohsome-api/commit/5bd6f4c2
[83c43fca]: https://github.com/GIScience/ohsome-api/commit/83c43fca
[86756acc]: https://github.com/GIScience/ohsome-api/commit/86756acc
[8d2e5685]: https://github.com/GIScience/ohsome-api/commit/8d2e5685
[8e170aa9]: https://github.com/GIScience/ohsome-api/commit/8e170aa9
[93b4e55d]: https://github.com/GIScience/ohsome-api/commit/93b4e55d
[97e984e7]: https://github.com/GIScience/ohsome-api/commit/97e984e7
[9aa0511b]: https://github.com/GIScience/ohsome-api/commit/9aa0511b
[9ca59338]: https://github.com/GIScience/ohsome-api/commit/9ca59338
[ab5295ca]: https://github.com/GIScience/ohsome-api/commit/ab5295ca
[b5ff6195]: https://github.com/GIScience/ohsome-api/commit/b5ff6195
[cc3061a3]: https://github.com/GIScience/ohsome-api/commit/cc3061a3
[d7107bcc]: https://github.com/GIScience/ohsome-api/commit/d7107bcc
[dd7d2a9e]: https://github.com/GIScience/ohsome-api/commit/dd7d2a9e
[f40f91d8]: https://github.com/GIScience/ohsome-api/commit/f40f91d8


## 0.9.6

### Prominent Changes

* showMetadata parameter can now accept true/false as input ([469b0316])

### New Features

* /users/count/groupBy/key ([e71b9902])

### Bugfixes

* Fixing wrong results for specific keys/values combinations using /share ([092b3c1c])
* Fixing too high results in some /users resources ([ec5626d1])

### Performance and Code Quality

* Faster /ratio computation ([0a991edb], [31ecae11])
* Removed duplicated code (e.g. [e6958eb8], [87a84d4b], [3a815731])
* Including of /share computation in /ratio processing ([6164ef85])
* Improving/fixing Javadoc comments and paths

### Other Changes

* ASCII representation of the HeiGIT logo in the console when running the jar file ([77295e1f])
* showMetadata and types input can be written in upper and lower case ([469b0316])
* Request URL in error response will only be included for GET requests ([fa0bf285])
* Spaces, empty lines, etc. are neglected in processing of bpolys and showMetadata input ([f559b9be])

[092b3c1c]: https://github.com/GIScience/ohsome-api/commit/092b3c1c
[0a991edb]: https://github.com/GIScience/ohsome-api/commit/0a991edb
[31ecae11]: https://github.com/GIScience/ohsome-api/commit/31ecae11
[3a815731]: https://github.com/GIScience/ohsome-api/commit/3a815731
[469b0316]: https://github.com/GIScience/ohsome-api/commit/469b0316
[469b0316]: https://github.com/GIScience/ohsome-api/commit/469b0316
[6164ef85]: https://github.com/GIScience/ohsome-api/commit/6164ef85
[77295e1f]: https://github.com/GIScience/ohsome-api/commit/77295e1f
[87a84d4b]: https://github.com/GIScience/ohsome-api/commit/87a84d4b
[e6958eb8]: https://github.com/GIScience/ohsome-api/commit/e6958eb8
[e71b9902]: https://github.com/GIScience/ohsome-api/commit/e71b9902
[ec5626d1]: https://github.com/GIScience/ohsome-api/commit/ec5626d1
[f559b9be]: https://github.com/GIScience/ohsome-api/commit/f559b9be
[fa0bf285]: https://github.com/GIScience/ohsome-api/commit/fa0bf285
