package fr.unice.polytech.codemera.statisticsservice.entities;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "orders")
public class Statistics {

    @Column(name = "time")
    private Instant time;

    @Column(name = "orderID")
    private int orderID;

    @Column(name = "orderStatus")
    private String status;

    @Override
    public String toString() {
        return "Statistics{" +
                "time=" + time +
                ", orderID=" + orderID +
                ", status='" + status + '\'' +
                '}';
    }
}
