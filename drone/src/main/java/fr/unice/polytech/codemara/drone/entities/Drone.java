package fr.unice.polytech.codemara.drone.entities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.Optional;

@Data
@Entity
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

    public Drone( double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean is(DroneStatus status) {
        return droneStatus == status;
    }
}
