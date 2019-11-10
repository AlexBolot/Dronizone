package fr.unice.polytech.dronemock.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

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

    private double distanceToPickup = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drone drone = (Drone) o;
        return droneID == drone.droneID &&
                Double.compare(drone.batteryLevel, batteryLevel) == 0 &&
                Double.compare(drone.distanceToPickup, distanceToPickup) == 0 &&
                droneStatus == drone.droneStatus &&
                whereabouts.equals(drone.whereabouts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(droneID, batteryLevel, droneStatus, whereabouts, distanceToPickup);
    }
}
