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

1. checkout/download the repository and import it as a Maven project in your IDE (or navigate in an editor to the class defined in step 2)
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
5. enter the command *java -jar target/oshdb-rest-api-0.0.1-SNAPSHOT.jar* to run the jar file

Now you should have a running local REST API, which is ready for receiving requests under *http://localhost:8080/*

## Testing

To be able to test the REST-API with your own requests, you will also need a description of the parameters and available resources. Both are given here below.

### Parameters


* bboxes
    * has to consist of double-parse able Strings in the format (lon1, lat1, lon2, lat2, meaning bottom left and top right point of each bbox)
    * if no bbox (and no other boundary parameter) is given, a default bbox representing the maximum extend (7.3948, 47.3937, 10.6139, 49.9079 for BW) is used
    * if bboxes is given, bpoints and bpolys must be null or empty
    * format: id1:x1,y1,x2,y2|id2:x1,y1,x2,y2|id3:x1,... OR x1,y1,x2,y2|x1,y1,x2,y2|x1,...
    * optional for all resources
* bpoints
    * has to consist of double-parse able Strings (lon/lat) + a double value representing the size of the buffer around the point
    * if bpoints is given, bboxes and bpolys must be null or empty
    * format: id1:x,y,r|id2:x,y,r|id3:x,... OR x,y,r|x,y,r|x,...
    * optional for all resources
* bpolys
    * has to consist of double-parse able lon/lat coordinate pairs, where the first point is the same as the last point
    * if bpolys is given, bboxes and bpoints must be null or empty
    * format: id1:x1,y1,x2,y2,... xn,yn,x1,y1|id2:x1,y1,x2,y2,... xm,ym,x1,y1|id3:x1,... OR x1,y1,x2,y2,... xn,yn,x1,y1|x1,y1,x2,y2,... xm,ym,x1,y1|x1,...
    * only simple polygons are supported atm (without holes and no multipolygon)
    * optional for all resources
* types
    * can be one, two or all three of "node", "way", "relation" in any order
    * if no type is given, all three are used
    * optional for all resources
* keys
    * 0...n keys can be used
    * if keys is null or empty, no key will be used (and values must also be null or empty)
    * optional for all resources
* values
    * 0...n values can be used, where n <= keys.length and values(n) must refer to keys(n)
    * if values is null or empty, no value will be used
    * optional for all resources
* userids
    * 0...n userids can be given
    * if userids is empty, all users are used (and the result is grouped on all affected userids in /groupBy/user)
    * optional for all resources
* time
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
    * if no time parameter is given, the most recent timestamp is used
    * the forward slashes (/) are a very important part of the parameter and used to recognize which time parameter should be used
    * an absence of the start and|or end timestamp when using a start-end pattern (e.g.: 2010-01-01//P6M) causes in using the earliest or latest timestamp available for the missing timestamp
    * more precise time parameters (using hours, minutes, seconds) are supported as well following the pattern  YYYY-MM-DDThh:mm:ss (e.g.: 2017-01-01T12:30:15)
    * '-MM-DD' or '-DD' as well as ':ss' can be omitted and will be replaced with '01' for month or day and '00' for seconds
    * optional for all resources
* showMetadata
    * can have the values 'true' or 'false'
    * if empty (or not defined), 'false' is used as default
    * optional for all resources
* types2
    * same format as types
    * optional in /ratio resource, not used in the others
* keys2
    * same format as keys
    * optional in /ratio and /share resources, not used in the others
* values2
    * same format as values
    * used in /ratio and /share requests
    * optional in /ratio and /share resources, not used in the others
* groupByKey
    * grouping by elements that have this key only
    * mandatory in /groupBy/tag resource, not used in the others
* groupByValues
    * 0...n groupByValues can be used, where n <= groupByKey.length and groupByValues(n) must refer to groupByKey(n)
    * optional in /groupBy/tag resource, not used in the others

### Implemented URIs

This gives you an overview of resources that are already implemented and can therefore be accessed.
All of them can be accessed with GET and POST requests, although it is recommended to use POST requests only if the length of the URL would exceed its limit (e.g. when using a lot of bboxes or complex polygons).
POST request data can only be sent in the format *application/x-www-form-urlencoded*.

* /elements
    * /count
        * /groupBy/boundary
        * /groupBy/type
        * /groupBy/tag
        * /groupBy/user
        * /share
        * /ratio
    * /length (for line features)
        * /groupBy/tag
        * /groupBy/user
        * /share
    * /perimeter (for polygonal features)
        * /groupBy/type
        * /groupBy/tag
        * /groupBy/user
        * /share
    * /area
        * /groupBy/type
        * /groupBy/tag
        * /groupBy/user
        * /share
    * /density

## Examples

This section gives you some example request URLs and shows the results returned by the REST API.

* http://localhost:8080/elements/count?bboxes=8.6128,49.3183,8.7294,49.4376&types=way&time=2008-01-01/2016-01-01/P2Y&keys=building&values=yes
<p> 
Gives the count within the given bounding box for all ways, which have the key “building” and the value “yes” for the time from 2008-01-01 till 2016-01-01 in a two year interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "result": [
        {
            "timestamp": "2008-01-01T00:00:00Z",
            "value": 1
        },
        {
            "timestamp": "2010-01-01T00:00:00Z",
            "value": 629
        },
        {
            "timestamp": "2012-01-01T00:00:00Z",
            "value": 9359
        },
        {
            "timestamp": "2014-01-01T00:00:00Z",
            "value": 16521
        },
        {
            "timestamp": "2016-01-01T00:00:00Z",
            "value": 20810
        }
    ]
}
```
<p>
* http://localhost:8080/elements/count/groupBy/boundary?bboxes=8.6128,49.3183,8.7294,49.4376|8.7128,49.4183,8.9294,49.5376&types=way&time=2015-01-01/2017-01-01/P1Y&keys=building&values=residential&showMetadata=true
<p> 
Gives the count grouped by the boundary objects for all ways, which have the key "building" and the value "residential" for the time from 2015-01-01 till 2017-01-01 in a yearly interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "metadata": {
        "executionTime": 7832,
        "unit": "amount",
        "description": "Total number of items aggregated on the boundary object.",
        "requestURL": "http://localhost:8080/elements/count/groupBy/boundary?bboxes=8.6128,49.3183,8.7294,49.4376%7C8.7128,49.4183,8.9294,49.5376&types=way&time=2015-01-01/2017-01-01/P1Y&keys=building&values=residential&showMetadata=true"
    },
    "groupByBoundaryResult": [
        {
            "groupByObject": "bbox1",
            "result": [
                {
                    "timestamp": "2015-01-01T00:00:00Z",
                    "value": 9577
                },
                {
                    "timestamp": "2016-01-01T00:00:00Z",
                    "value": 10656
                },
                {
                    "timestamp": "2017-01-01T00:00:00Z",
                    "value": 10911
                }
            ]
        },
        {
            "groupByObject": "bbox2",
            "result": [
                {
                    "timestamp": "2015-01-01T00:00:00Z",
                    "value": 926
                },
                {
                    "timestamp": "2016-01-01T00:00:00Z",
                    "value": 1037
                },
                {
                    "timestamp": "2017-01-01T00:00:00Z",
                    "value": 1058
                }
            ]
        }
    ]
}
```
<p>
* http://localhost:8080/elements/count/ratio?bpolys=8.6128,49.3183,8.6130,49.3956,8.7294,49.4376,8.7302,49.3512,8.6128,49.3183&types=way&time=2009-05-01/2017-05-01/P2Y&keys=building&types2=node&keys2=addr:housenumber
<p>
Gives the values and the ratio within the given bounding polygon for all nodes with the key "addr:housenumber" compared to the total number of objects with the type "way" and the key "building" for the time from 2009-05-01 till 2017-05-01 in a two year interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "ratioResult": [
        {
            "timestamp": "2009-05-01T00:00:00Z",
            "value": 297,
            "value2": 130,
            "ratio": 0.4377104377104377
        },
        {
            "timestamp": "2011-05-01T00:00:00Z",
            "value": 5889,
            "value2": 1354,
            "ratio": 0.22992019018509086
        },
        {
            "timestamp": "2013-05-01T00:00:00Z",
            "value": 17204,
            "value2": 1422,
            "ratio": 0.08265519646593815
        },
        {
            "timestamp": "2015-05-01T00:00:00Z",
            "value": 25265,
            "value2": 1943,
            "ratio": 0.07690480902434198
        },
        {
            "timestamp": "2017-05-01T00:00:00Z",
            "value": 27818,
            "value2": 2080,
            "ratio": 0.0747717305341865
        }
    ]
}
```

## Built With

* [Eclipse](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/oxygen1a) - IDE
* [Spring Boot](https://projects.spring.io/spring-boot/) - Web framework
* [Maven](https://maven.apache.org/) - Dependency management and project building

## Tested With

* [Postman](https://www.getpostman.com/) - Software to test REST APIs (build and send HTTP requests and view the responses)

## Authors


## License


## Acknowledgments

