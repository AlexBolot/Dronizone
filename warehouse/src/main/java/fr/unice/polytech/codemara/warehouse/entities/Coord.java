package fr.unice.polytech.codemara.warehouse.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Embeddable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Embeddable
public class Coord {

    private double longitude;
    private double latitude;

    public Coord() {
    }

    public Coord(double lon, double lat) {
        this.longitude = lon;
        this.latitude = lat;
    }
}
