# INTEGRATION TESTER
_This module is aimed at providing simple intégration test process with all the classical test tooling for the dronazone project_
## Structure
All test are located in `src/test/java/*` and filename should begin with `IT`

All acceptation scenarios should be found in `src/test/resources/features/**.feature` and constitute the core of the testing framework
Matching step defs can be found in the test location

## Usage
To run the test simply run `mvn clean verify`

You can also run tests through classical tooling such as intelij, however be carefull to run all pre and post test steps specified in the `pom.xml` by yourself, ( for now it consists of `docker-compose up`and `docker-compose down`)
### Before testing
Make sure that every module has been build, as the integration testing does not include rebuilding of our mvn modules.
### Side effects
The test running through maven launches a docker-compose configuration, this can be heavy for the running host as it is composed of many containers.
It will also be shut down but not destroyed post execution to allow for test debugging
 
