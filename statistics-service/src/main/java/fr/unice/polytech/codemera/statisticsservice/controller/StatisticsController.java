package fr.unice.polytech.codemera.statisticsservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/stats/", produces = "application/json")
public class StatisticsController {

    private static final String ORDER_DELIVERED = "order-delivered";
    private static final String ORDER_PACKED = "order-packed";
    private static final String ORDER_ID_TAG_NAME = "orderID";
    private static final String ORDER_STATUS_TAG_NAME = "orderStatus";

    private final KafkaTemplate kafkaTemplate;

    @Autowired
    private InfluxDB influxDB;

    public StatisticsController(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @KafkaListener(topics = ORDER_PACKED, groupId = "stat-service")
    public void listenForOrderPacked(String content) {
        Gson gson = new Gson();
        OrderStatusMessage osm = gson.fromJson(content, OrderStatusMessage.class);
        int orderID = osm.getOrder_id();
        influxDB.setDatabase("dronazone");
        Point point = Point.measurement("orders")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(ORDER_ID_TAG_NAME, orderID)
                .addField(ORDER_STATUS_TAG_NAME, osm.getStatus())
                .build();
        influxDB.write(point);
    }

    @KafkaListener(topics = ORDER_DELIVERED, groupId = "stat-service")
    public void listenForOrderDeliver(String content) {
        Gson gson = new Gson();
        OrderStatusMessage osm = gson.fromJson(content, OrderStatusMessage.class);
        String orderStatus = osm.getStatus();
        int orderID = osm.getOrder_id();
        influxDB.setDatabase("dronazone");
        Point point = Point.measurement("orders")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(ORDER_ID_TAG_NAME, orderID)
                .addField(ORDER_STATUS_TAG_NAME, orderStatus)
                .build();
        influxDB.write(point);
    }

    @GetMapping("/testget")
    public String testGetData() {
        Query query = new Query("Select * from orders", "dronazone");
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<Statistics> statisticsList = resultMapper
                .toPOJO(influxDB.query(query), Statistics.class);
        return statisticsList.toString();
    }

    @GetMapping("/testput")
    public String testPutData() {
        influxDB.setDatabase("dronazone");
        Point point = Point.measurement("orders")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(ORDER_ID_TAG_NAME, 1)
                .addField(ORDER_STATUS_TAG_NAME, Status.DELIVERED.toString())
                .build();
        influxDB.write(point);
        Point point2 = Point.measurement("test")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(ORDER_ID_TAG_NAME, 2)
                .addField(ORDER_STATUS_TAG_NAME, Status.DELIVERED.toString())
                .build();
        influxDB.write(point2);
        Point point3 = Point.measurement("test")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(ORDER_ID_TAG_NAME, 3)
                .addField(ORDER_STATUS_TAG_NAME, Status.DELIVERED.toString())
                .build();
        influxDB.write(point3);
        return "ok";
    }

    @GetMapping("/testpublish")
    public String testPublishData() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("orderid", "id");
        parameters.put("status", ORDER_PACKED);
        try {
            kafkaTemplate.send("order-packed", new ObjectMapper().writeValueAsString(parameters));
            return "ok";
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "ko";
        }


    }

}