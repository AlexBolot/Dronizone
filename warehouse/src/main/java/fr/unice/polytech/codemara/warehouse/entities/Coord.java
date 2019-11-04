package fr.unice.polytech.codemara.warehouse.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
public class Coord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int orderId;


    private double longitude;
    private double latitude;

    public Coord() {
    }

    public Coord(double lon, double lat) {
        this.longitude = lon;
        this.latitude = lat;
    }
}
