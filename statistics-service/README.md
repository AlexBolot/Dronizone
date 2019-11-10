# STATISTICS SERVICE
## Usage
Be sure to set the following env variables to run the service
- INFLUX_HOST : The host of the influx db relative to the running service
- INFLUX_USERNAME : The username of the influx db usually `dronazone` in dev environements
- INFLUX_PWD: the password of the influx db usually same as username in dev envs

### Influx Queries and insertion
The influx connection was build based on [this tutorial](https://www.baeldung.com/java-influxdb)

The influx connection has been Beanified, you can use @Autowired or put it in constructors to get it

### Tests
To test the correct fonctionnement of this service, REST routes were created and implemented in StatisticsController
- stats/testpublish : sends the message of an order packed in correct kafka topic, that is listened by this service
- stats/testput : register 3 Points (data) in influxdb
- stats/testget : return the data in influxdb

Cucumber tests were implemented, but we had trouble to test with Kafka and Influx. 
We lost a considerable amount of time trying to fix these tests, so we decided to give them up, and to put these REST routes.
The *.feature file is commented, and the StepDefs are still accessible.