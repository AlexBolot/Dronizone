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
For proper influx mocking during unit and local test use the spring profile "test" ( see example in `StatisticsServiceApplicationTests`)
