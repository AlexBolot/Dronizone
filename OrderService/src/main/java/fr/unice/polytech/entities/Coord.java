package fr.unice.polytech.entities;

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
    private int id;
    private String lon;
    private String lat;

    public Coord() {
    }

    public Coord(String lon, String lat) {
        this.lon = lon;
        this.lat = lat;
    }
}
