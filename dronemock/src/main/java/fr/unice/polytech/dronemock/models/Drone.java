package fr.unice.polytech.dronemock.models;

import fr.unice.polytech.dronemock.models.DroneStatus;
import fr.unice.polytech.dronemock.models.Whereabouts;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
public class Drone {
    @Id
    @GeneratedValue
    private long droneID;
    private double batteryLevel;
    private DroneStatus droneStatus;
    @ManyToOne
    private Whereabouts whereabouts;
    @OneToOne
    public Delivery currentDelivery;

    public Drone(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean is(DroneStatus status) {
        return droneStatus == status;
    }
}
