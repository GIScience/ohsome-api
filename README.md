# OSHDB Web REST API

This REST API aims to leverage the tools of the [oshdb Java API](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/oshdb/core) through allowing to access some of its functionalities via HTTP requests.
Click [here](https://confluence.gistools.geog.uni-heidelberg.de/display/oshdb/Web+Rest+API) to read information about the whole planning process behind this REST API.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher
* [Apache Maven 3.5](https://maven.apache.org/download.cgi) or higher
* atm for local testing as well: IDE like [Eclipse](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/oxygen1a), or an editor that can open .java files like [Notepad++](https://notepad-plus-plus.org/download/v7.5.4.html)
* data: keytables.mv.db and baden-wuerttemberg.mv.db (available at *veeam.geog.uni-heidelberg.de\gis2\oshdb-data*)

### Installing

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

## Implemented URIs

This section gives you an overview about which resources can already be accessed (state 2017-12-22).

* /elements/count
* /elements/count/groupBy/bbox (atm still quite slow for more bboxes)
* /elements/count/groupBy/type
* /elements/count/groupBy/user
* /elements/length
* /elements/length/groupBy/user
* /elements/perimeter
* /elements/perimeter/groupBy/type
* /elements/perimeter/groupBy/user
* /elements/area
* /elements/area/groupBy/type
* /elements/area/groupBy/user
* /elements/density
* /elements/ratio

## Examples

This section gives you some example request URLs and shows the results returned by the REST API.

* http://localhost:8080/elements/count?bboxes=8.6128,49.3183,8.7294,49.4376&types=relation&time=2014-01-01/2017-07-01/P6M&keys=building&values=yes
<p> 
Gives the count within the given bounding box for all relations, which have the key “building” and the value “yes” for the time from 2014-01-01 till 2017-07-01 in a six-months interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "metaData": {
        "executionTime": 5727,
        "unit": "amount",
        "description": "Total number of elements, which are selected by the parameters."
    },
    "result": [
        {
            "timestamp": "2014-01-01T00:00:00Z",
            "value": "34"
        },
        {
            "timestamp": "2014-07-01T00:00:00Z",
            "value": "36"
        },
        {
            "timestamp": "2015-01-01T00:00:00Z",
            "value": "42"
        },
        {
            "timestamp": "2015-07-01T00:00:00Z",
            "value": "49"
        },
        {
            "timestamp": "2016-01-01T00:00:00Z",
            "value": "53"
        },
        {
            "timestamp": "2016-07-01T00:00:00Z",
            "value": "51"
        },
        {
            "timestamp": "2017-01-01T00:00:00Z",
            "value": "45"
        },
        {
            "timestamp": "2017-07-01T00:00:00Z",
            "value": "54"
        }
    ]
}
```
<p>
* http://localhost:8080/elements/count/groupBy/type?bboxes=8.6128,49.3183,8.7294,49.4376&types=way,relation&time=2012-01-01/2015-01-01/P1Y&keys=building
<p> 
Gives the count grouped by the type within the given bbox for all ways and relations, which have the key "building" for the time from 2012-01-01 till 2015-07-01 in a yearly interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "metaData": {
        "executionTime": 5504,
        "unit": "amount",
        "description": "Total number of items aggregated on the types."
    },
    "groupByResult": [
        {
            "groupByObj": "WAY",
            "result": [
                {
                    "timestamp": "2012-01-01T00:00:00Z",
                    "value": "9570"
                },
                {
                    "timestamp": "2013-01-01T00:00:00Z",
                    "value": "17461"
                },
                {
                    "timestamp": "2014-01-01T00:00:00Z",
                    "value": "28406"
                },
                {
                    "timestamp": "2015-01-01T00:00:00Z",
                    "value": "34508"
                }
            ]
        },
        {
            "groupByObj": "RELATION",
            "result": [
                {
                    "timestamp": "2012-01-01T00:00:00Z",
                    "value": "43"
                },
                {
                    "timestamp": "2013-01-01T00:00:00Z",
                    "value": "49"
                },
                {
                    "timestamp": "2014-01-01T00:00:00Z",
                    "value": "37"
                },
                {
                    "timestamp": "2015-01-01T00:00:00Z",
                    "value": "49"
                }
            ]
        }
    ]
}
```
<p>
* http://localhost:8080/elements/ratio?bpolys=8.6128,49.3183,8.6130,49.3956,8.7294,49.4376,8.7302,49.3512,8.6128,49.3183&types=way&time=2009-11-01/2017-11-01/P1Y&keys=highway&values=residential&types2=way&keys2=highway,maxspeed&values2=residential
<p>
Gives the ratio within the given bounding polygon for all residential highways with a maxspeed compared to the total number of residential highways for the time from 2009-05-01 till 2017-05-01 in a yearly interval.

```json
{
    "license": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
    "copyright": "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
    "metaData": {
        "executionTime": 8867,
        "unit": "ratio",
        "description": "Ratio of items satisfying types2, keys2, values2 within items are selected by types, keys, values."
    },
    "result": [
        {
            "timestamp": "2009-05-01T00:00:00Z",
            "value": "0.3161232"
        },
        {
            "timestamp": "2010-05-01T00:00:00Z",
            "value": "0.3473054"
        },
        {
            "timestamp": "2011-05-01T00:00:00Z",
            "value": "0.4011076"
        },
        {
            "timestamp": "2012-05-01T00:00:00Z",
            "value": "0.41004497"
        },
        {
            "timestamp": "2013-05-01T00:00:00Z",
            "value": "0.4556597"
        },
        {
            "timestamp": "2014-05-01T00:00:00Z",
            "value": "0.51816314"
        },
        {
            "timestamp": "2015-05-01T00:00:00Z",
            "value": "0.5421687"
        },
        {
            "timestamp": "2016-05-01T00:00:00Z",
            "value": "0.60271317"
        },
        {
            "timestamp": "2017-05-01T00:00:00Z",
            "value": "0.67108583"
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

