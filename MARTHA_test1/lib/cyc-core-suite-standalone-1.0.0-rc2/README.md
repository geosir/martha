Core API Standalone Release Bundler 1.0.0-rc2
================================

This project bundles the [Cyc Core API Suite](https://github.com/cycorp/CycCoreAPI) along with
[example code](https://github.com/cycorp/CoreAPIUseCases) and a compiled jar file containing all 
Core API classes and dependencies.

For more information, visit the [Cyc Developer Center](http://dev.cyc.com/).


Requirements
------------

### Java

* `JDK 1.7` or greater to build; code can be run on Java 6.
* [Apache Maven](http://maven.apache.org/), version `3.2` or higher. If you are new to Maven, you 
  may wish to view the [quick start](http://maven.apache.org/run-maven/index.html).

### Cyc Server

#### CycCoreAPI

The CycCoreAPI currently supports the following Cyc server releases:

* **ResearchCyc 4.0q** or higher.
* **EnterpriseCyc 1.7-preview** or higher.

The Core API Suite is _not_ presently compatible with any current release of **OpenCyc.**

#### CoreAPIUseCases

The examples within the CoreAPIUseCases project are intended to be run against **ResearchCyc 4.0q** 
or higher. All of the code demonstrated is compatible with **EnterpriseCyc 1.7-preview** or higher, 
but note that, by design, EnterpriseCyc does not contain the _KB content_ necessary to run the 
specific examples.

For inquiries about obtaining a suitable version of ResearchCyc or EnterpriseCyc, please visit the
[Cyc Dev Center download page](http://dev.cyc.com/cyc-api/download.html).


Manifest
--------

Unpacking the archive, you should see the following structure:

* cyc-core-suite-standalone-1.0.0-rc2/
    * CoreAPITests/
    * CoreAPIUseCases/
    * CycCoreAPI/
    * lib/
        * cyc-core-suite-1.0.0-rc2-jar-with-dependencies.jar
    * LICENSE
    * README.md

**CoreAPITests**: A project containing the source code for all of the unit tests from the Core API 
                  Suite, and the dependencies necessary to run them.

**CoreAPIUseCases**: A Maven project containing code examples of common usage of the Core API Suite.

**CycCoreAPI**: A Maven project containing the source code for the Core API Suite, along with
                instructions for building it.

**lib**: Jar file directory. It contains **cyc-core-suite-1.0.0-rc2-jar-with-dependencies.jar**, 
         a jar file containing the Core API Suite classes and dependencies.

**LICENSE**: Software license.

**README.md**: This README file.

For changes, see `CycCoreAPI/CHANGELOG.md`


Installation
------------

From inside the `cyc-core-suite-standalone-1.0.0-rc2` directory, run the following:

    mvn install:install-file -Dfile=lib/cyc-core-suite-1.0.0-rc2-jar-with-dependencies.jar \
                             -DpomFile=lib/cyc-core-suite.pom


Running the test suite
----------------------

The `CoreAPITests` directory is a buildable Maven project which contains the source code for all of
the unit tests in the Core API suite. Instructions for building and running the test suite, along 
with examples for how to run individual tests, may be found in `CoreAPITests/README.md`.


