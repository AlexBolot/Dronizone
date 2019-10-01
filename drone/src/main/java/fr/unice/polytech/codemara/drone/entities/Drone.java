package fr.unice.polytech.codemara.drone.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public
class Drone {
    private String droneID;
    private double batteryLevel;
    private DroneStatus droneStatus;
    private Whereabouts whereabouts;

    public Drone() {
    }

    public Drone(String droneID, double batteryLevel) {
        this.droneID = droneID;
        this.batteryLevel = batteryLevel;
    }

    boolean is(DroneStatus status) {
        return droneStatus == status;
    }
}
