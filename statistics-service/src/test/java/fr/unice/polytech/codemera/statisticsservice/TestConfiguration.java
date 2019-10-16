package fr.unice.polytech.codemera.statisticsservice;

import org.influxdb.InfluxDB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.mock;

@Profile("test")
@Configuration
public class TestConfiguration {
    @Bean
    @Primary
    public InfluxDB influxDB(Environment env) {
        return mock(InfluxDB.class);

    }
}
