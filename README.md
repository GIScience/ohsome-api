# OHSOME API

This REST-API aims to leverage the tools of the [OSHDB-API](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/ohsome/oshdb) through allowing to access some of its functionalities via HTTP requests.
Click [here](https://confluence.gistools.geog.uni-heidelberg.de/display/oshdb/Web+Rest+API) to read information about the whole planning process behind this REST-API.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher
* [Apache Maven 3.5](https://maven.apache.org/download.cgi) or higher
* data: e.g. baden-wuerttemberg.oshdb.mv.db (available at *veeam.geog.uni-heidelberg.de\gis2\oshdb-data* or click [here](https://confluence.gistools.geog.uni-heidelberg.de/display/oshdb/How+to+set+up+the+database+locally) to see a guide how to download new data yourself)

### Setting-up/Running

1. checkout/download the repository
2. move to your Maven project directory in a shell (e.g. Windows PowerShell)
3. enter the command *mvn package* to build the project
4. to run the jar file enter the following (changes depending on your data):
    * keytables included (v_0.3.1): *java -jar target/ohsome-api-0.0.1-SNAPSHOT.jar --database.db=C:\\path-to-your-data\\ba-wue.oshdb*
    * keytables not included (v_0.3): *java -jar target/ohsome-api-0.0.1-SNAPSHOT.jar --database.db=C:\\path-to-your-data\\ba-wue.oshdb --database.keytables=C:\\path-to-your-keytablesFile\\keytables*

Now you should have a running local REST-API, which is ready for receiving requests under *http://localhost:8080/*.

Note:
* additionally you can add an optional run-parameter to disable multithreading: *--database.multithreading=false*
* if you want to run the maven project in your IDE, you need to set the paths to your data in the run configurations
    * in Eclipse: Run As --> Run Configurations --> (x)= Arguments --> Program arguments: 'enter the parameters here'
* if you want to get information about the code directly, you can access the [Javadoc](http://129.206.7.121:8044/master/ohsome-api/target/site/apidocs/index.html), which gets updated daily.

## Testing

To be able to test the REST API with your own requests, you will also need a description of the parameters and available resources. 
Both are given in the [Swagger2](http://localhost:8080/swagger-ui.html#/) documentation, which can be accessed while your local OHSOME-API copy is running.
It lists all available resources and gives detailled information about the individual input parameters and JSON responses.

## Examples

This section gives you some example request URLs and shows the returned JSON responses.

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
    "groupByBoundaryMetadata": {
        "executionTime": 8448,
        "unit": "amount",
        "boundary": {
            "bbox2": [
                8.7128,
                49.4183,
                8.9294,
                49.5376
            ],
            "bbox1": [
                8.6128,
                49.3183,
                8.7294,
                49.4376
            ]
        },
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

