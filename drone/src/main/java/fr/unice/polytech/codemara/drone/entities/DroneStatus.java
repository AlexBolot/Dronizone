package fr.unice.polytech.codemara.drone.entities;

import java.util.Optional;

public enum DroneStatus {
    ACTIVE,
    ASIDE,
    CALLED_HOME;

    public static Optional<DroneStatus> find(String statusName) {
        try {
            return Optional.of(DroneStatus.valueOf(statusName.toUpperCase()));
        } catch (IllegalArgumentException iae) {
            return Optional.empty();
        }
    }
}
