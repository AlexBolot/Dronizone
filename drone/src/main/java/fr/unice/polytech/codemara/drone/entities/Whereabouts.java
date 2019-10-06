package fr.unice.polytech.codemara.drone.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
//@Entity
//@Data
public class Whereabouts {
    private double latitude;
    private double longitude;
    private double altitude;
    private double distanceToTarget;
}

