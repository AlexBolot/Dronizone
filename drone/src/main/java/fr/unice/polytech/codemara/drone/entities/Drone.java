package fr.unice.polytech.codemara.drone.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
//@Entity
//@Data
public class Drone {

    //@Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private String droneID;
    private double batteryLevel;
    private Status status;
    private Whereabouts whereabouts;

    public Drone() {
    }

    public Drone(String droneID, double batteryLevel) {
        this.droneID = droneID;
        this.batteryLevel = batteryLevel;
    }

    boolean is(Status status) {
        return this.status == status;
    }

    public enum Status {
        ACTIVE,
        CALLED_HOME,
        ASIDE;

        public static Optional<Status> find(String statusName) {
            try {
                return Optional.of(Status.valueOf(statusName.toUpperCase()));
            } catch (IllegalArgumentException iae) {
                return Optional.empty();
            }
        }
    }
}
