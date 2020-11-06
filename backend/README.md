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

We are using mostly IntelliJ IDE to develop the project. Currently, using
**IntelliJ 2019.3.**

First make sure you have the correct versions of Java and Maven installed on your machine. Notice that IntelliJ bundles a version of Maven that might be different from your local version. This is not necessarily a project.

Open up IntelliJ and select `Import Project`, then choose `Maven` and then
choose the `backend` folder in this repository. The project should be imported correctly but the Maven window will probably display this failure:

    Failure to find junit:junit:pom:5.5.2 in https://repo.maven.apache.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced

Don't worry the project should build, and the tests run. It's complaining because of the dependency tree but it's working.

### Plugins

In order to install plugins, go to `Settings > Build, Execution, Deployment > Compiler > Annotation Processors` and select `Enable annotation processing`.

We are using:
 * **[Lombok](https://projectlombok.org/)** project for code generation. In order for the IDE to recognize the annotations we need to enable the annotations preprocessor and the Lombok plugin.
 * **[SonarLint](https://www.sonarlint.org/)** highlights Bugs and Security Vulnerabilities as you write code. Before pushing the code into the repository, it's recommended to run SonarLint to solve possible issues.

Then go to `Settings > Plugins` and search for `Lombok` and `SonarLint` in the Marketplace tab. Install them and restart the IDE.

From now, no more warnings should be displayed in the `Project` window.

⚠️ **Before running the backend locally, the docker-compose file under backend/docker/local directory must be up to provide the database/localstack dependencies** 

You can run the docker compose file from the IDE or from the terminal with `docker-compose up -d` but you must be in the directory of docker-compose file.

## Build the project

From the terminal you can build the project with `mvn package`.

## Run the API

Open a new terminal with `mvn clean compile spring-boot:run` to launch the backend on port 8080. Remember don't close the terminal if you need use the API.

Notice that the platform can also be run directly from the IDE by just right clicking in the `Application.java`or `App.java` file which might be more convenient in some cases.

## Run the tests

The docker daemon must already be running in your system in order to run the integrations tests that require it.

From the IDE you may run the tests with the right mouse button on the `src/test` folder.

## Run the documentation of the API in Swagger

If don't have the API running. Open a new terminal with `mvn clean compile spring-boot:run` to launch the backend on port 8080. Remember don't close the terminal if you need use the API.

Notice that the platform can also be run directly from the IDE by just right clicking in the `Application.java`or `App.java` file which might be more convenient in some cases.

On the another hand the documentation is disable with the profile `prod`, only works in `dev` and `local`profiles.

* [Swagger dev](https://dev.huelladigital.ayudadigital.org/swagger-ui/)
* [Swagger local](http://localhost:8080/swagger-ui)

## Handling errors
* _Migration checksum mismatch for migration version 1.0.x_: It may appear during `mvn clean compile spring-boot:run`, and it happens due to conflicts with migration versions of the Flyway. In order to fix this error, you have two options:
  * Open terminal and type 
    * `docker rm <container_id>` using as <container_id> the id corresponding to the container with IMAGE _postgres_ and NAMES _local_huellapositiva_database_1_ (you can obtain the id by typing `docker ps -a`).
    * `docker volume rm local_postgres-data` which deletes your local postgres configuration.
    * `docker-compose up -d` (the flag _-d_ runs the command in background).
  * In case you have IntelliJ Ultimate, you can manually delete the tables of the database (locally).
  
  Then, you can restart the project, and the latest migration version will be loaded.
* _cloud.localstack.docker.exception.LocalstackDockerException: Could not start the localstack docker container_: 
  * Restart docker containers. Afterwards, lift up only **local_huellapositiva_database_1** (postgres).
  * Be sure that your environment variable 'Path' includes both paths to your Docker and Docker Desktop bin folders. 
  They are usually found at:
     * "C:\Program Files\Docker\Docker\resources\bin"
     * "C:\ProgramData\DockerDesktop\version-bin"
  * Create a new system environment variable "DOCKER_LOCATION". Its value should be the path to docker.exe. It is usually found at "C:\Program Files\Docker\Docker\resources\docker.exe".

## Useful information
* [How to start backend and frontend to use backend API](https://airanschez.wordpress.com/2020/06/06/cronicas-del-proyecto-huella-digital-parte-1/)

## Information for frontend developers
The project has different roles:
* VOLUNTEER: He can see the list of published proposals and join them.
* VOLUNTEER_NOT_CONFIRMED: He can see the list of published proposals. (He must confirm the email in order to join a proposal)
* CONTACT_PERSON: He can see the list of published proposals with the volunteers that have joined it. He is allowed to create proposals, modify them and request their cancellation.
* CONTACT_PERSON_NOT_CONFIRMED: He can see the list of published proposals with the volunteers that have joined it. He is allowed to create proposals, modify them and request their cancellation. (He must confirm the email)
* REVISER: He can see the list of published proposals with the volunteers. He is allowed to create proposals, review, modify and cancel them. 
