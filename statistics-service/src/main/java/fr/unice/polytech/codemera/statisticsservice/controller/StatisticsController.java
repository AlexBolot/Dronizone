package fr.unice.polytech.codemera.statisticsservice.controller;

import fr.unice.polytech.codemera.statisticsservice.entities.OrderStatusMessage;
import fr.unice.polytech.codemera.statisticsservice.entities.Statistics;
import fr.unice.polytech.codemera.statisticsservice.entities.Status;
import gherkin.deps.com.google.gson.Gson;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/stats/", produces = "application/json")
public class StatisticsController {

    private final KafkaTemplate kafkaTemplate;

    @Autowired
    private InfluxDB influxDB;

    public StatisticsController(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/hello")
    public String order_ping() {
        return "Hello";
    }

    @KafkaListener(topics = "orders")
    public void listenForOrderUpdate(String content) {
        Gson gson = new Gson();
        OrderStatusMessage osm = gson.fromJson(content, OrderStatusMessage.class);
        Status orderStatus = Status.valueOf(osm.getStatus());
        int orderID = osm.getOrder_id();
        Point point = Point.measurement("orders")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("orderID", orderID)
                .addField("orderStatus", orderStatus.toString())
                .build();
        influxDB.write(point);
    }

    @GetMapping("/test")
    public String testinflux() {
        Query query = new Query("Select * from orders", "dronazone");
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<Statistics> statisticsList = resultMapper
                .toPOJO(influxDB.query(query), Statistics.class);
        return statisticsList.toString();

    }

    @GetMapping("/testpeupler")
    public String testPut() {
        influxDB.setDatabase("dronazone");
        Point point = Point.measurement("orders")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("orderID", 1)
                .addField("orderStatus", Status.DELIVERED.toString())
                .build();
        influxDB.write(point);
        Point point2 = Point.measurement("orders")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("orderID", 2)
                .addField("orderStatus", Status.DELIVERED.toString())
                .build();
        influxDB.write(point2);
        Point point3 = Point.measurement("orders")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("orderID", 3)
                .addField("orderStatus", Status.DELIVERED.toString())
                .build();
        influxDB.write(point3);
        return "ok";
    }

}