# Backend: Huella Positiva

The backend is being developed with:

* Java 11
* Spring Boot 2.2.5
* Maven 3.6.0

## Setup the local development environment

We are using mostly IntelliJ IDE to develop the project. Currently using
IntelliJ 2019.3.

First make sure you have the correct versions of Java and Maven installed on your machine. Notice that IntelliJ bundles a version of Maven that might be different from your local version. This is not necessarily a project.

Open up IntelliJ and select `Import Project`, then choose `Maven` and then
choose the `backend` folder in this repository. The project should be imported correctly but the Maven window will probably display this failure:

    Failure to find junit:junit:pom:5.5.2 in https://repo.maven.apache.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced

Don't worry the project should build and the tests run. It's complaining because of the dependency tree but it's working.

We are using Lombok project for code generation. In order for the IDE to recognize the annotations we need to enable the annotations preprocessor and the Lombok plugin.

Go to `Settings > Build, Execution, Deployment > Compiler > Annotation Processors` and select `Enable annotation processing`.

Then go to `Settings > Plugins` and search for `Lombok` in the Marketplace tab. Install the first one and restart the IDE.

From now, no more warnings should be displayed in the `Project` window.

From the terminal you can build the project with `mvn package`. From the IDE you may run the tests with the right mouse button on the `src/test` folder.




