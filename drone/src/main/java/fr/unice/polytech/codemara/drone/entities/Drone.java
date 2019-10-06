package fr.unice.polytech.codemara.drone.entities;

import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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

    public Drone( double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    boolean is(DroneStatus status) {
        return droneStatus == status;
    }
}
