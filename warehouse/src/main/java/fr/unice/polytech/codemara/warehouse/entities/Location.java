package fr.unice.polytech.codemara.warehouse.entities;

import lombok.*;

import javax.persistence.Embeddable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Embeddable
public class Location {

    private double longitude;
    private double latitude;

}
