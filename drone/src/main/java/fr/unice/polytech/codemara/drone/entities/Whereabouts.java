package fr.unice.polytech.codemara.drone.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
public class Whereabouts {
    @Id
    @GeneratedValue
    private long id;
    @Embedded
    private Location location;
    private double altitude;
    private double distanceToTarget;

    public Whereabouts() {
        this.location = new Location();
    }
}

