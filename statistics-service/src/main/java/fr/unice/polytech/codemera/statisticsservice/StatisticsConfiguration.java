package fr.unice.polytech.codemera.statisticsservice;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Profile("!test")
@Configuration
public class StatisticsConfiguration {
    @Bean
    public InfluxDB influxDB(Environment env) {
        return InfluxDBFactory.connect(
                Objects.requireNonNull(env.getProperty("INFLUX_HOST")),
                Objects.requireNonNull(env.getProperty("INFLUX_USERNAME")),
                env.getProperty("INFLUX_PWD")
        );
    }

}
