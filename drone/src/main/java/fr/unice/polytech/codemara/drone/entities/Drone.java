package fr.unice.polytech.codemara.drone.entities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

import static java.lang.Double.MAX_VALUE;

@Data
@Entity
@RequiredArgsConstructor
public class Drone {
    @Id
    @GeneratedValue
    private long droneID;
    private double batteryLevel;
    private double maxWeight;
    private DroneStatus droneStatus;
    @ManyToOne
    private Whereabouts whereabouts;
    @OneToOne
    public Shipment currentShipment;
    @OneToOne
    private Delivery currentDelivery;

    public Drone(double batteryLevel) {
        this(batteryLevel, MAX_VALUE);
    }

    public Drone(double batteryLevel, double maxWeight) {
        this.batteryLevel = batteryLevel;
        this.maxWeight = maxWeight;
    }

    public boolean is(DroneStatus status) {
        return droneStatus == status;
    }
}
