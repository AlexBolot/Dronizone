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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/stats/", produces = "application/json")
public class StatisticsController {

    private static final String ORDER_DELIVERED = "order-delivered";
    private static final String ORDER_PACKED = "order-packed";
    private static final String ORDER_ID_TAG_NAME = "orderID";
    private static final String ORDER_STATUS_TAG_NAME = "orderStatus";

    @Autowired
    private InfluxDB influxDB;


    @KafkaListener(topics = ORDER_PACKED)
    public void listenForOrderPacked(String content) {
        Gson gson = new Gson();
        OrderStatusMessage osm = gson.fromJson(content, OrderStatusMessage.class);
        Status orderStatus = Status.valueOf(osm.getStatus());
        int orderID = osm.getOrder_id();
        Point point = Point.measurement(ORDER_PACKED)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(ORDER_ID_TAG_NAME, orderID)
                .addField(ORDER_STATUS_TAG_NAME, orderStatus.toString())
                .build();
        //System.out.println("LALALALALALALALA JE SUIS LAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + influxDB);
        influxDB.write(point);
    }

    @KafkaListener(topics = ORDER_DELIVERED)
    public void listenForOrderDeliver(String content) {
        Gson gson = new Gson();
        OrderStatusMessage osm = gson.fromJson(content, OrderStatusMessage.class);
        Status orderStatus = Status.valueOf(osm.getStatus());
        int orderID = osm.getOrder_id();
        Point point = Point.measurement(ORDER_DELIVERED)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField(ORDER_ID_TAG_NAME, orderID)
                .addField(ORDER_STATUS_TAG_NAME, orderStatus.toString())
                .build();
        //System.out.println("LALALALALALALALA JE SUIS LAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + influxDB);
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
        Point point = Point.measurement("test")
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

}