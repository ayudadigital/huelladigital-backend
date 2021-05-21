[![CI/CD](https://github.com/ayudadigital/huelladigital-backend/actions/workflows/cicd.yml/badge.svg?branch=develop)](https://github.com/ayudadigital/huelladigital-backend/actions/workflows/cicd.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=alert_status)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=coverage)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ayudadigital_huelladigital&metric=security_rating)](https://sonarcloud.io/dashboard?id=ayudadigital_huelladigital)

# Backend: Huella Positiva

The backend is being developed with:

-   Java 11
-   Spring Boot 2.2.5
-   Maven 3.6.0

We are using mostly IntelliJ IDE to develop the project. Currently, using
**IntelliJ 2019.3.**

## Setup the local development environment

First make sure you have the correct versions of Java and Maven installed on your machine. Notice that IntelliJ bundles a version of Maven that might be different from your local version.

Clone the repo:

```sh
git clone https://github.com/ayudadigital/huelladigital-backend.git
```

Inside **IntelliJ** `New Project > Import Project From External Sources` (as Maven) and choose the `backend` folder in this repo.

Maven might complain with:

    Failure to find junit:junit:pom:5.5.2 in https://repo.maven.apache.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced

The project should build anyway, and the tests run. It's complaining because of the dependency tree but it works.

Make sure to set the Java compiler to v11: `Settings > Build, Execution, Deployment > Compiler > Java Compiler > Target bytecode version`

### Plugins

In order to install plugins, go to `Settings > Build, Execution, Deployment > Compiler > Annotation Processors` and select `Enable annotation processing`.

We are using:

-   **[Lombok](https://projectlombok.org/)** project for code generation. In order for the IDE to recognize the annotations we need to enable the annotations preprocessor and the Lombok plugin. (Included with newer versions od IntelliJ)
-   **[SonarLint](https://www.sonarlint.org/)** highlights Bugs and Security Vulnerabilities as you write code. Before pushing the code into the repository, it's recommended to run SonarLint to solve possible issues.

Go to `Settings > Plugins` and search for `Lombok` and `SonarLint` in the Marketplace tab. Install them and restart the IDE.

From now on, no more warnings should be displayed in the `Project` window.

## Build the project

-   Terminal: `cd` into the project directory (backend dir within huelladigital-backend) and run `mvn package`.

or

-   IntelliJ: in the Maven tab, `platform > Lifecycle > package`

## Run the project locally

⚠️ **Before running the backend locally** ⚠️

-   Make sure that docker is enabled and started
-   Run `docker-compose up -d` from console in `backend/docker/local` to provide the database/localstack dependencies

Just run the project from within IntelliJ and go to `http://localhost:8080/actuator/health`. Expect to see `{"status","up"}`.

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

-   [Swagger dev](https://dev.huelladigital.ayudadigital.org/swagger-ui/)
-   [Swagger local](http://localhost:8080/swagger-ui)

## Handling errors

-   _Migration checksum mismatch for migration version 1.0.x_: It may appear during `mvn clean compile spring-boot:run`, and it happens due to conflicts with migration versions of the Flyway. In order to fix this error, you have two options:
    -   Open terminal and type
        -   `docker rm <container_id>` using as <container*id> the id corresponding to the container with IMAGE \_postgres* and NAMES _local_huellapositiva_database_1_ (you can obtain the id by typing `docker ps -a`).
        -   `docker volume rm local_postgres-data` which deletes your local postgres configuration.
        -   `docker-compose up -d` (the flag _-d_ runs the command in background).
    -   In case you have IntelliJ Ultimate, you can manually delete the tables of the database (locally).
    Then, you can restart the project, and the latest migration version will be loaded.
-   _cloud.localstack.docker.exception.LocalstackDockerException: Could not start the localstack docker container_:
    -   Restart docker containers. Afterwards, lift up only **local_huellapositiva_database_1** (postgres).
    -   Be sure that your environment variable 'Path' includes both paths to your Docker and Docker Desktop bin folders.
        They are usually found at:
        -   "C:\Program Files\Docker\Docker\resources\bin"
        -   "C:\ProgramData\DockerDesktop\version-bin"
    -   Create a new system environment variable "DOCKER_LOCATION". Its value should be the path to docker.exe. It is usually found at "C:\Program Files\Docker\Docker\resources\docker.exe".
    -   Restart the IDE to enable the new configuration.

## Useful information

-   [How to start backend and frontend to use backend API](https://airanschez.wordpress.com/2020/06/06/cronicas-del-proyecto-huella-digital-parte-1/)

# Information for frontend developers

The project has different roles:

-   VOLUNTEER: He can see the list of published proposals and join them.
-   VOLUNTEER_NOT_CONFIRMED: He can see the list of published proposals. (He must confirm the email in order to join a proposal)
-   CONTACT_PERSON: He can see the list of published proposals with the volunteers that have joined it. He is allowed to create proposals, modify them and request their cancellation.
-   CONTACT_PERSON_NOT_CONFIRMED: He can see the list of published proposals with the volunteers that have joined it. He is allowed to create proposals, modify them and request their cancellation. (He must confirm the email)
-   REVISER: He can see the list of published proposals with the volunteers. He is allowed to create proposals, review, modify and cancel them.

## Endpoints for frontend

-   Change status volunteer in proposal. Endpoint POST http://localhost:8080/api/v1/proposals/changeStatusVolunteerProposal.
    We do not send an email in MVP version, it will be added in future versions.
    This method is POST, don´t forget to use the access token as Bearer Token and use the XSRF-TOKEN, copy and paste in HEADER as X-XSRF-TOKEN.
    Steps to use this endpoint:
          1º. Register a contact person.
          2º. Login with contact person.
          3º. Register ESAL.
          4º. Register a proposal.
          5º. Register a volunteer.
          6º. Login with volunteer.
          7º. Join volunteer in the proposal.
          8º. Login with contact person.
          9º. Use this method.

-   Cancel proposal as reviser. Endpoint POST http://localhost:8080/api/v1/proposals/{id}/status/cancel
    Steps to test in postman:
          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register a new ESAL.
          4ª. Create a proposal.
          5ª. Login as reviser.
          6ª. Use this method.

-   Fetch Proposal With Volunteers. Endpoint GET http://localhost:8080/api/v1/proposals/{idProposal}/proposal

    Steps to test in postman:

          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register a new ESAL.
          4ª. Create a proposal.
          5ª. Register a new Volunteer.
          6ª. Login as volunteer.
          7ª. Join the proposal.
          8ª. Login as reviser or contact person.
          9ª. Use this method.


-   Fetch listed volunteers in proposal. Endpoint GET http://localhost:8080/api/v1/proposals/{idProposal}/volunteers

    This steps require the access token. Steps to test in postman:

          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register a new ESAL.
          4ª. Create a proposal.
          5ª. Register a new Volunteer.
          6ª. Login as volunteer.
          7ª. Join the proposal.
          8ª. Login as reviser or contact person.
          9ª. Use this method.

-   Submit proposal revision. Endpoint POST http://localhost:8080/api/v1/proposals/revision/{id}

    This steps require the access token and the XSRF-TOKEN. Steps to test in postman:

          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register a new ESAL.
          4ª. Create a proposal.
          5ª. Login as reviser.
          6ª. Use this method.

-   Fetch listed proposals. Endpoint GET http://localhost:8080/api/v1/proposals/{page}/{size}/reviser

    This steps require the access token. Steps to test in postman:

          1ª. Login as reviser.
          2ª. Register a new ESAL.
          3ª. Create a new Proposal.
          4ª. Use this method.


-   Fetch listed published proposals. Endpoint GET http://localhost:8080/api/v1/proposals/{page}/{size}

    This steps require the access token Steps to test in postman:

          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register a new ESAL.
          4ª. Create a new proposal.
          5ª. Use this method.


-   Create proposal as reviser. Endpoint POST http://localhost:8080/api/v1/proposals/reviser

    This steps require the access token. Steps to test in postman:

          1ª. Login as reviser.
          2ª. Register a new ESAL.
          3ª. Use this method.


-   Join proposal. Endpoint POST http://localhost:8080/api/v1/proposals/{id}/join

    This steps require the access token. Steps to test in postman:

          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register an Esal.
          4ª. Register a new proposal.
          5ª. Register a new volunteer.
          6ª. Login as volunteer.
          7ª. Use this method.


-   Get proposal. Endpoint GET http://localhost:8080/api/v1/proposals/{id}

    This steps require the access token. Steps to test in postman:

          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register an Esal.
          4ª. Register a new proposal.
          5ª. Use this method.


-   Create proposal. Endpoint POST http://localhost:8080/api/v1/proposals

    This steps require the access token and the XSRF-TOKEN. Steps to test in postman:

          1ª. Register a contact person.
          2ª. Login as contact person.
          3ª. Register an Esal.
          4ª. Use this method.


-   Update photo. Endpoint POST http://localhost:8080/api/v1/volunteers/photo-upload

    This steps require the access token and the XSRF-TOKEN. Steps to test in postman:

          1ª. Register a Volunteer.
          2ª. Login as Volunteer.
          3ª. Use this method.


-   Fetch profile information volunteer. Endpoint GET http://localhost:8080/api/v1/volunteers/fetchProfileInformation

    This steps require the access token. Steps to test in postman:

          1ª. Register a Volunteer.
          2ª. Login as Volunteer.
          3ª. Use this method.


-   Update profile information volunteer. Endpoint POST http://localhost:8080/api/v1/volunteers/updateProfileInformation

    This steps require the access token and the XSRF-TOKEN. Steps to test in postman:

          1ª. Register a Volunteer.
          2ª. Login as Volunteer.
          3ª. Use this method.
