# OSHDB Web REST API

This REST API aims to leverage the tools of the [oshdb Java API](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/oshdb/core) through allowing to access some of its functionalities via HTTP requests.
Click [here](https://confluence.gistools.geog.uni-heidelberg.de/display/oshdb/Web+Rest+API) to read information about the whole planning process behind this REST API and [here](http://129.206.7.121:8044/rest-api/target/site/apidocs/index.html) to access the javadoc, which gets updated daily.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher
* [Apache Maven 3.5](https://maven.apache.org/download.cgi) or higher
* atm for local testing as well: IDE like [Eclipse](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/oxygen1a), or an editor that can be used to modify .java files like [Notepad++](https://notepad-plus-plus.org/download/v7.5.4.html)
* data: keytables.mv.db and baden-wuerttemberg.mv.db (available at *veeam.geog.uni-heidelberg.de\gis2\oshdb-data* or click [here](https://confluence.gistools.geog.uni-heidelberg.de/display/oshdb/How+to+set+up+the+database+locally) to see a guide how to download new data yourself)

### Setting-up/Running

1. checkout/download the repository and import it as a Maven project in your IDE
2. go to the class ContextRefreshedListener.java in the package listener and change the following paths to your local directories of the db files and make sure to exclude the file endings '.mv.db' from the path

    ```java
        @Override
        public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
            System.out.println("Context Event Received");
            eventHolderBean.dbConn("C:/yourPath/baden-wuerttemberg.oshdb",
    				"C:/yourPath/keytables", true);
    ```
3. move to your Maven project directory in a shell (e.g. Windows PowerShell)
4. enter the command *mvn package* to build the project
5. enter the command *java -jar target/springBootWebAPI-0.0.1-SNAPSHOT.jar* to run the jar file

Now you should have a running local REST API, which is ready for receiving requests under *http://localhost:8080/*

## Testing

To be able to test the REST-API with your own requests, you will also need a description of the parameters and available resources. Both are given here below.

### Parameters


* bbox
    * has to consist of double-parse able Strings in the format (lon1, lat1, lon2, lat2, meaning bottom left and top right point of each bbox)
    * if no bbox (and no other boundary parameter) is given, a default bbox representing the maximum extend (7.3948, 47.3937, 10.6139, 49.9079 for BW) is used
    * if bbox is given, bpoint and bpoly must be null or empty
* bpoint
    * has to consist of double-parse able Strings (lon/lat) + a double value representing the size of the buffer around the point
    * if bpoint is given, bbox and bpoly must be null or empty
* bpoly
    * has to consist of double-parse able lon/lat coordinate pairs, where the first point is the same as the last point
    * if bpoly is given, bbox and bpoint must be null or empty
* types
    * can be one, two or all three of "node", "way", "relation" in any order
    * if no type is given, all three are used
* keys
    * 0...n keys can be used
    * if keys is null or empty, no key will be used (and values must also be null or empty)
* values
    * 0...n values can be used, where n <= keys.length and values(n) must refer to keys(n)
    * if values is null or empty, no value will be used
* userids
    * 0...n userids can be given
    * if userids is empty in .../groupBy/user then all affected users are used
* time
    * if no time parameter is given, the most recent timestamp is used
    * ten different versions of the time parameter can be provided:
        1. timestamp: YYYY-MM-DD
        2. start/end: YYYY-MM-DD/YYYY-MM-DD
        3. start/end/period: YYYY-MM-DD/YYYY-MM-DD/PnYnMnD where n refers to the size of the respective period
        4. /end: /YYYY-MM-DD where ‘null’/ equals the earliest timestamp
        5. /end/period: /YYYY-MM-DD/PnYnMnD
        6. start/: YYYY-MM-DD/ where /’null’ equals the latest timestamp
        7. start//period: YYYY-MM-DD//PnYnMnD
        8. /: / where ‘null’/’null’ equals the earliest and latest timestamp
        9. //period: //PnYnMnD
        10. list of 2-n timestamps separated via a “,” e.g.: 2015-01-01,2015-05-15,2016-03-18
    * the forward slashes (/) are a very important part of the parameter and used to recognize which time parameter should be used
    * an absence of the start and|or end timestamp when using a start-end pattern (e.g.: 2010-01-01//P6M) causes in using the earliest or latest timestamp available for the missing timestamp
    * more precise time parameters (using hours, minutes, seconds) are supported as well following the pattern  YYYY-MM-DDThh:mm:ss (e.g.: 2017-01-01T12:30:15)
* types2
    * same format as types
    * used in /ratio requests
* keys2
    * same format as keys
    * used in /ratio and /share requests
* values2
    * same format as values
    * used in /ratio and /share requests
* groupByKey
    * grouping by elements that have this key only
    * used in /groupBy/tag
* groupByKeys
    * grouping by elements that have these keys
    * used in groupBy/key
* groupByValues
    * 0...n groupByValues can be used, where n <= groupByKey.length and groupByValues(n) must refer to groupByKey(n)
    * used in groupBy/tag

### Implemented URIs

This gives you an overview of resources that are already implemented and can therefore be accessed (state 2018-01-15).
All of them can be accessed with GET and POST requests, although it is recommended to use POST requests only if the length of the URL would exceed its limit (e.g. when using a lot of bboxes or complex polygons).
POST request data can only be sent in the format *application/x-www-form-urlencoded*.

* /elements/count
* /elements/count/groupBy/bbox (atm still quite slow for more bboxes)
* /elements/count/groupBy/type
* /elements/count/groupBy/tag
* /elements/count/groupBy/user
* /elements/count/share
* /elements/count/ratio
* /elements/length
* /elements/length/groupBy/tag
* /elements/length/groupBy/user
* /elements/length/share
* /elements/perimeter
* /elements/perimeter/groupBy/type
* /elements/perimeter/groupBy/tag
* /elements/perimeter/groupBy/user
* /elements/perimeter/share
* /elements/area
* /elements/area/groupBy/type
* /elements/area/groupBy/tag
* /elements/area/groupBy/user
* /elements/area/share
* /elements/density

## Examples

This section gives you some example request URLs and shows the results returned by the REST API.

* http://localhost:8080/elements/count?bboxes=8.6528,49.3683,8.7294,49.4376&types=way&time=2008-01-01/2016-01-01/P2Y&keys=building&values=yes
<p> 
Gives the count within the given bounding box for all ways, which have the key “building” and the value “yes” for the time from 2008-01-01 till 2016-01-01 in a two year interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "metaData": {
        "executionTime": 4499,
        "unit": "amount",
        "description": "Total number of elements, which are selected by the parameters.",
        "requestURL": "http://localhost:8080/elements/count?bboxes=8.6528,49.3683,8.7294,49.4376&types=way&time=2008-01-01/2016-01-01/P2Y&keys=building&values=yes"
    },
    "result": [
        {
            "timestamp": "2008-01-01T00:00:00Z",
            "value": "1"
        },
        {
            "timestamp": "2010-01-01T00:00:00Z",
            "value": "451"
        },
        {
            "timestamp": "2012-01-01T00:00:00Z",
            "value": "6590"
        },
        {
            "timestamp": "2014-01-01T00:00:00Z",
            "value": "10961"
        },
        {
            "timestamp": "2016-01-01T00:00:00Z",
            "value": "13299"
        }
    ]
}
```
<p>
* http://localhost:8080/elements/count/groupBy/type?bboxes=8.6128,49.3183,8.7294,49.4376&types=way,relation&time=2013-01-01/2014-01-01/P1Y&keys=building
<p> 
Gives the count grouped by the type within the given bbox for all ways and relations, which have the key "building" for the time from 2013-01-01 till 2014-01-01 in a yearly interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "metaData": {
        "executionTime": 5171,
        "unit": "amount",
        "description": "Total number of items aggregated on the userids.",
        "requestURL": "http://localhost:8080/elements/count/groupBy/type?bboxes=8.6128,49.3183,8.7294,49.4376&types=way,relation&time=2013-01-01/2014-01-01/P1Y&keys=building"
    },
    "groupByResult": [
        {
            "groupByObj": "WAY",
            "result": [
                {
                    "timestamp": "2013-01-01T00:00:00Z",
                    "value": "17461"
                },
                {
                    "timestamp": "2014-01-01T00:00:00Z",
                    "value": "28406"
                }
            ]
        },
        {
            "groupByObj": "RELATION",
            "result": [
                {
                    "timestamp": "2013-01-01T00:00:00Z",
                    "value": "49"
                },
                {
                    "timestamp": "2014-01-01T00:00:00Z",
                    "value": "37"
                }
            ]
        }
    ]
}
```
<p>
* http://localhost:8080/elements/ratio?bpolys=8.6128,49.3183,8.6130,49.3956,8.7294,49.4376,8.7302,49.3512,8.6128,49.3183&types=way&time=2009-11-01/2017-11-01/P2Y&keys=highway&values=residential&types2=way&keys2=highway,maxspeed&values2=residential
<p>
Gives the ratio within the given bounding polygon for all residential highways with a maxspeed compared to the total number of residential highways for the time from 2009-05-01 till 2017-05-01 in a two year interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "metaData": {
        "executionTime": 9058,
        "unit": "ratio",
        "description": "Ratio of items satisfying types2, keys2, values2 within items are selected by types, keys, values.",
        "requestURL": "http://localhost:8080/elements/ratio?bpolys=8.6128,49.3183,8.6130,49.3956,8.7294,49.4376,8.7302,49.3512,8.6128,49.3183&types=way&time=2009-11-01/2017-11-01/P2Y&keys=highway&values=residential&types2=way&keys2=highway,maxspeed&values2=residential"
    },
    "result": [
        {
            "timestamp": "2009-11-01T00:00:00Z",
            "value": "0.32243815"
        },
        {
            "timestamp": "2011-11-01T00:00:00Z",
            "value": "0.4064665"
        },
        {
            "timestamp": "2013-11-01T00:00:00Z",
            "value": "0.48918355"
        },
        {
            "timestamp": "2015-11-01T00:00:00Z",
            "value": "0.5744681"
        },
        {
            "timestamp": "2017-11-01T00:00:00Z",
            "value": "0.6723485"
        }
    ]
}
```

## Built With

* [Eclipse](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/oxygen1a) - IDE
* [Spring Boot](https://projects.spring.io/spring-boot/) - Web framework
* [Maven](https://maven.apache.org/) - Dependency management and project building

## Tested With

* [Postman](https://www.getpostman.com/) - Software to test REST APIs (e.g. build and send HTTP requests and view the responses)

## Authors


## License


## Acknowledgments

