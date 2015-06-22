Core API Tests
==============

This project contains the source code for all of the unit tests from the Core API Suite, and the 
dependencies necessary to run them.

There are two ways you may run this project:

1. Eclipse
2. Apache Maven

Note that Apache Maven will need to retrieve project dependencies from the Internet if they are not
already installed in your local repository. The Eclipse project requires no Internet connectivity.


Using Eclipse
-------------

From within Eclipse, select `File > Import > Existing Projects into Workspace`. Navigate to the 
`CoreAPITests` directory and click `Finish`.

The `CoreAPITests` project should appear in your Package Explorer. Right-click on it, and select 
`Run As > JUnit Test`. When prompted to select a test configuration, choose `CoreAPITests-default`.
A dialog will pop up, asking you to select a Cyc server against which the tests should be run.


Using Maven
-----------

Note that Apache Maven will need to retrieve project dependencies from the Internet if they are not
already installed in your local repository.

### Running all tests

From inside the `CoreAPITests` directory, run the following:

    mvn clean test -Dcyc.session.server=[HOST_NAME]:[BASE_PORT]

E.g.:

    mvn clean test -Dcyc.session.server=localhost:3600

Test results will be displayed to the console, and saved in `target/surefire-reports/`.

Note that specifying the `clean` goal will cause Maven to delete the `target` directory, and any
existing reports and compiled unit tests, before re-compiling and running the test suite. If you do
not wish to delete to delete the target directory, you can omit the `clean` goal. E.g.:

    mvn test -Dcyc.session.server=localhost:3600


### Running individual tests

You may run a single test class from the command line by passing its simple class name as a property:

    mvn clean test -Dtest=[NAME_OF_TEST] -Dcyc.session.server=[HOST_NAME]:[BASE_PORT]

E.g.:

    mvn clean test -Dtest=ConfigurationLoaderManagerTest -Dcyc.session.server=localhost:3600

You may run a single test method:

    mvn clean test -Dtest=CycServerTest#testIsDefined -Dcyc.session.server=localhost:3600

You may also use patterns to run a number of tests:

    mvn clean test -Dtest=SessionManagerImplTest,Configuration*Test -Dcyc.session.server=localhost:3600

For more information, see the [Maven SureFire Plugin examples](http://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html).
