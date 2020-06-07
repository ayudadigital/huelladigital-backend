[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=alert_status)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=coverage)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=security_rating)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)

# Backend: Huella Positiva

The backend is being developed with:

* Java 11
* Spring Boot 2.2.5
* Maven 3.6.0

## Setup the local development environment

We are using mostly IntelliJ IDE to develop the project. Currently using
**IntelliJ 2019.3.**

First make sure you have the correct versions of Java and Maven installed on your machine. Notice that IntelliJ bundles a version of Maven that might be different from your local version. This is not necessarily a project.

Open up IntelliJ and select `Import Project`, then choose `Maven` and then
choose the `backend` folder in this repository. The project should be imported correctly but the Maven window will probably display this failure:

    Failure to find junit:junit:pom:5.5.2 in https://repo.maven.apache.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced

Don't worry the project should build and the tests run. It's complaining because of the dependency tree but it's working.

We are using **Lombok** project for code generation. In order for the IDE to recognize the annotations we need to enable the annotations preprocessor and the Lombok plugin.

Go to `Settings > Build, Execution, Deployment > Compiler > Annotation Processors` and select `Enable annotation processing`.

Then go to `Settings > Plugins` and search for `Lombok` in the Marketplace tab. Install the first one and restart the IDE.

From now, no more warnings should be displayed in the `Project` window.

⚠️ **Before running the the backend locally, the docker-compose file under backend/docker/local directory must be up to provide the database/localstack dependencies** 

You can run the docker compose file from the IDE or from the terminal with `docker-compose up -d` but you must be in the directory of docker-compose file.

## Build the project

From the terminal you can build the project with `mvn package`.

## Run the API

Open a new terminal with `mvn clean compile spring-boot:run` to launch the backend on port 8080. Remember don't close the terminal if you need use the API.

Notice that the platform can also be run directly from the IDE by just right clicking in the `Application.java`or `App.java` file which might be more convenient in some cases.

## Run the tests

The docker daemon must already be running in your system in order to run the integrations tests that require it.

From the IDE you may run the tests with the right mouse button on the `src/test` folder.
