package fr.unice.polytech.codemara.drone.entities;

import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
public class Drone {
    @Id
    @GeneratedValue
    private long droneID;
    private double batteryLevel;
    private Status status;
    @ManyToOne
    private Whereabouts whereabouts;
    @OneToOne
    public Delivery currentDelivery;

    public Drone( double batteryLevel) {
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
