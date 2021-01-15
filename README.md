# ohsome API

[![Build Status](http://jenkins.ohsome.org/buildStatus/icon?job=ohsome-api/master)](http://jenkins.ohsome.org/blue/organizations/jenkins/ohsome-api/activity)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.heigit.ohsome/ohsome-api/badge.svg)](https://search.maven.org/artifact/org.heigit.ohsome/ohsome-api)
[![LICENSE](https://img.shields.io/github/license/GIScience/ohsome-api)](LICENSE)
[![API docs](https://img.shields.io/badge/API-docs-blue.svg)](https://docs.ohsome.org/ohsome-api/stable)
[![JavaDocs](https://img.shields.io/badge/Java-docs-blue.svg)](https://docs.ohsome.org/java/ohsome-api)
[![status: active](https://github.com/GIScience/badges/raw/master/status/active.svg)](https://github.com/GIScience/badges#active)

The ohsome API is a generic web API for in-depth analysis of OpenStreetMap (OSM) data with a focus on it's history. It allows to get aggregated statistics about the evolution of OSM data itself and about the contributors behind the data. Furthermore, data extraction methods are provided to access the historic development of individual OSM features.

The functionalities of the ohsome API can be accessed via HTTP requests. As a basis underneath serves the [OSHDB API](https://github.com/GIScience/oshdb). The current stable version is [v1.3.0](https://github.com/GIScience/ohsome-api/releases/tag/1.3.0). Developed and maintained by [HeiGIT](https://heigit.org/).

## Using the ohsome API

To make your life easier, we already have a running ohsome API instance on our servers, where you can send your requests to analyze the history of the OpenStreetMap data. This instance is publicly accessible under the following URL:

https://api.ohsome.org/v1 (current stable version)

This URL automatically redirects you to the documentation page, where you find explanations and examples for all the different parameters and endpoints that we have implemented in the API. We also have a blog post series called [how to become ohsome](http://k1z.blog.uni-heidelberg.de/tag/become-ohsome/), which gives diverse example analysis and updates on new features. Through the [swagger UI](https://api.ohsome.org/v1/swagger-ui.html) page of the ohsome API you can send simple GET requests and test the individual endpoints.

If you want to contribute to the code base of the ohsome API, please follow the guideline and hints in the upcoming sections.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* [Java 11](https://openjdk.java.net/projects/jdk/11/) or higher
* [Apache Maven 3.5](https://maven.apache.org/download.cgi) or higher
* [Lombok 1.18.16](https://projectlombok.org/download) or higher. Please check the [requirements](https://projectlombok.org/setup/overview) for your IDE 
* data: [download](http://downloads.ohsome.org/) it directly, or work through a guide on [how to prepare a new OSHDB extract](https://github.com/GIScience/oshdb/blob/master/oshdb-tool/etl/README.md)

### Setting-up/Running

1. checkout/download the repository
2. move to your Maven project directory in a shell (e.g. Windows PowerShell)
3. enter the command `mvn -DskipTests=true package` to build the project (if you want to build it running the integrated tests too, look at the section [Testing](#testing))
4. to run the jar file enter the following (if no additional keytables file is given, you can assume that it is included):
    * keytables included: `java -jar target/ohsome-api-1.3.0.jar --database.db=C:/path-to-your-data/ba-wue.oshdb`
    * keytables not included: `java -jar target/ohsome-api-1.3.0.jar --database.db=C:/path-to-your-data/ba-wue.oshdb --database.keytables=C:/path-to-your-keytablesFile/keytables`

Now you should have a running local API, which is ready for receiving requests under *http://localhost:8080/*.
<br>To check if it is running properly, you should be able to visit the swagger documentation under *http://localhost:8080/swagger-ui.html*.

*Note:*
* additionally you can add optional run-parameters:
    * to disable multithreading: `--database.multithreading=false`
    * to enable in-memory-caching: `--database.caching=true` (caution.. enabling this option requires quite some memory, but makes processing much faster)
* if you want to run the maven project in your IDE, you need to set the paths to your data in the run configurations
    * in Eclipse: *Run As --> Run Configurations --> (x)= Arguments --> Program arguments: 'enter the parameters here'*
* if you want to get information about the code directly, you can access the [Javadoc](https://docs.ohsome.org/java/ohsome-api/), which gets updated daily.

## Testing

To run the tests locally, you need the following:
1. define the properties `-Dport_get -Dport_post -Dport_data` using three free ports (for example 8081, 8082, 8083), which the API will use to start instances and run different integration tests
    * -Dport.get starts data-aggregation + metadata tests using GET requests
    * -Dport.post starts data-aggregation tests using POST requests
    * -Dport.data starts data-extraction tests using GET and POST requests
2. [heidelberg.oshdb](https://downloads.ohsome.org/v0.6/europe/germany/baden-wuerttemberg/heidelberg_68900_2020-07-23.oshdb.mv.db) file (or any other, which includes the data from Heidelberg)
3. maven command: `mvn -Dport_get=8081 -Dport_post=8082 -Dport_data=8083 -DdbFilePathProperty="--database.db=<path-to-your-heidelberg.oshdb-file>" test`

*Note:* 
* You can disable the integration and/or junit tests via the following properties: `-Dintegration="no" -Djunit="no"`
* If you do not define the -Dport_xyz property, the corresponding test class will not be executed

## Examples

This section gives you an overview on analysis and services, that were/are using the ohsome API, as well as a JSON response example.<p>
<p>


<p>
The following blog posts describe analysis, which were using the ohsome API:
   
* [Farm shops are ohsome](http://k1z.blog.uni-heidelberg.de/2019/07/05/farm-shops-are-ohsome/)
* [Plausible Parrots - HeiGIT’s OSHDB Supports Research in Citizen Science Data Quality](http://k1z.blog.uni-heidelberg.de/2019/02/27/plausible-parrots-heigits-oshdb-supports-research-in-citizen-science-data-quality/)
* [Visualizing the historical OSM evolution of your city](http://k1z.blog.uni-heidelberg.de/2018/12/14/how-to-become-ohsome-part-1-visualizing-the-historical-evolution-of-osm-buildings-of-your-city/) 
* [Exploring OSM history: the example of health related amenities](http://k1z.blog.uni-heidelberg.de/2019/05/16/exploring-osm-history-the-example-of-health-realted-amenities/)
* several posts of the [how to become ohsome](http://k1z.blog.uni-heidelberg.de/tag/become-ohsome/) series

<p>
These services are using the ohsome API:
   
* [ohsomeHeX](https://ohsome.org/apps/osm-history-explorer/#/amenity_clinic_healthcare_clinic_ptpl/2020-06-01T00:00:00Z/2/0/0)
* [ohsome dashboard](https://ohsome.org/apps/dashboard/)
* [ohsome2label](https://github.com/GIScience/ohsome2label)


Here you see an example response giving the length of residential roads for a bounding box around the german city Heidelberg.

```json
{
    "attribution": {
        "url": "https://ohsome.org/copyrights",
        "text": "© OpenStreetMap contributors"
    },
    "apiVersion": "1.3.0",
    "metadata": {
        "executionTime": 858,
        "description": "Total length of items in meters.",
        "requestUrl": "http://localhost:8080/elements/length?bboxes=8.6128,49.3183,8.7294,49.4376&time=2010-01-01/2016-08-01/P2Y2M2D&showMetadata=true&filter=highway=residential%20and%20type:way"
    },
    "result": [
        {
            "timestamp": "2010-01-01T00:00:00Z",
            "value": 344220.86
        },
        {
            "timestamp": "2012-03-03T00:00:00Z",
            "value": 352116.48
        },
        {
            "timestamp": "2014-05-05T00:00:00Z",
            "value": 351579.81
        },
        {
            "timestamp": "2016-07-07T00:00:00Z",
            "value": 350577.72
        }
    ]
}
```

## Documentation

[Install Sphinx](https://www.sphinx-doc.org/en/master/usage/installation.html) before running the following commands e.g. by using this commands:
```bash
cd docs
pip3 install -r requirements.txt
```

To update the ohsome API swagger files for the documentation:
```bash
cd docs
wget 'https://api.ohsome.org/v1/docs?group=Data%20Aggregation' -O _static/swagger-aggregation.json
wget 'https://api.ohsome.org/v1/docs?group=Data%20Extraction' -O _static/swagger-extraction.json
wget 'https://api.ohsome.org/v1/docs?group=Metadata' -O _static/swagger-metadata.json
```

The documentation can be built with the following command:
```bash
cd docs
make clean # if you want to recreate all pages
make html
```

If you want to see the release version of the ohsome API docs, use this environment variable:
```bash
cd docs
DOCS_DEPLOYMENT=release make clean html
```

## Built With

* [Spring Boot](https://projects.spring.io/spring-boot/) - Web framework
* [Maven](https://maven.apache.org/) - Dependency management and project building
* [Sphinx](https://www.sphinx-doc.org) - API documentation

## Tested With

* [Postman](https://www.getpostman.com/) - Software to test REST APIs (build and send HTTP requests and view the responses)

## License

[AGPL](LICENSE).
