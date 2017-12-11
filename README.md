# OSHDB Web REST API

This REST API aims to leverage the tools of the [oshdb](https://gitlab.gistools.geog.uni-heidelberg.de/giscience/big-data/oshdb/core) through allowing to access some of its functionalities via HTTP requests.
Click [here](https://confluence.gistools.geog.uni-heidelberg.de/display/oshdb/Web+Rest+API) for reading information about the whole planning, parameter formats, etc. for this REST API.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher
* [Apache Maven 3.5](https://maven.apache.org/download.cgi) or higher
* atm for local testing as well: IDE (e.g. [Eclipse](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/oxygen1a))
* data: keytables.mv.db and baden-wuerttemberg.mv.db (available at *veeam.geog.uni-heidelberg.de\gis2\oshdb-data*)

### Installing

1. check out the repository and import it as a Maven project in your IDE
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

Now you have a running local REST API, which is ready for receiving requests under *http://localhost:8080/*

## Examples

This section will give you some example request URLs and show you the results returned by the REST API.

## Built With

* [Eclipse](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/oxygen1a) - IDE
* [Spring Boot](https://projects.spring.io/spring-boot/) - Web framework
* [Maven](https://maven.apache.org/) - Dependency management and project building

## Tested With

* [Postman](https://www.getpostman.com/) - Software to test REST APIs (e.g. build and send HTTP requests)

## Authors


## License


## Acknowledgments

