package fr.unice.polytech.dronemock.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@AllArgsConstructor
public class Whereabouts {
    @Embedded
    private Location location;
    private double altitude;
    private double distanceToTarget;
}
