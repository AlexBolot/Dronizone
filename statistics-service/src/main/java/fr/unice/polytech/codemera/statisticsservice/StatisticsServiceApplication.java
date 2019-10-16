package fr.unice.polytech.codemera.statisticsservice;

import org.influxdb.InfluxDB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class StatisticsServiceApplication {

    private final KafkaTemplate<String, String> kafka;

    public StatisticsServiceApplication(InfluxDB influxDB, KafkaTemplate<String, String> kafkaTemplate) {
        this.influxDB = influxDB;
        this.kafka = kafkaTemplate;
        this.kafka.getDefaultTopic();
    }

    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }

    final InfluxDB influxDB;
}
