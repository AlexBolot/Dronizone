package fr.unice.polytech.dronemock.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Drone {

    private long droneID;

    private double batteryLevel;

    private DroneStatus droneStatus;

    private Whereabouts whereabouts;

    public Drone(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean is(DroneStatus status) {
        return droneStatus == status;
    }
}
