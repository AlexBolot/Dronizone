package fr.unice.polytech.entities;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Coord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String lon;

    private String lat;

    public Coord() {}

    public Coord(String lon, String lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public int getId() {
        return id;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coord coord = (Coord) o;

        if (getId() != coord.getId()) return false;
        if (getLon() != null ? !getLon().equals(coord.getLon()) : coord.getLon() != null) return false;
        return getLat() != null ? getLat().equals(coord.getLat()) : coord.getLat() == null;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (getLon() != null ? getLon().hashCode() : 0);
        result = 31 * result + (getLat() != null ? getLat().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Coord{" +
                "id=" + id +
                ", lon='" + lon + '\'' +
                ", lat='" + lat + '\'' +
                '}';
    }
}
