package fr.unice.polytech.codemara.drone.entities;

import lombok.*;
import org.aspectj.lang.annotation.DeclareAnnotation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Whereabouts {
    @Id
    @GeneratedValue
    private long id;
    private double latitude;
    private double longitude;
    private double altitude;
    private double distanceToTarget;
}

