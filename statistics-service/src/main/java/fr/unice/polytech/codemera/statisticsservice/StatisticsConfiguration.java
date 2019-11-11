package fr.unice.polytech.codemera.statisticsservice;

import com.google.common.base.Predicates;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Objects;

@Profile("!test")
@Configuration
@EnableSwagger2
public class StatisticsConfiguration {
    @Bean
    public InfluxDB influxDB(Environment env) {
        return InfluxDBFactory.connect(
                Objects.requireNonNull(env.getProperty("INFLUX_HOST")),
                Objects.requireNonNull(env.getProperty("INFLUX_USERNAME")),
                env.getProperty("INFLUX_PWD")
        );
    }


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build();
    }

}
