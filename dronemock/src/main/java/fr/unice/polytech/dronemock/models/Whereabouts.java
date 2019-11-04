package fr.unice.polytech.dronemock.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Embedded;

@Data
@AllArgsConstructor
public class Whereabouts {
    @Embedded
    private Location location;
    private double altitude;
    private double distanceToTarget;
}
