package fr.unice.polytech.dronemock.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Embedded;
import java.util.Objects;

@Data
@AllArgsConstructor
public class Whereabouts {
    @Embedded
    private Location location;
    private double altitude;
    private double distanceToTarget;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Whereabouts that = (Whereabouts) o;
        return Double.compare(that.altitude, altitude) == 0 &&
                Double.compare(that.distanceToTarget, distanceToTarget) == 0 &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, altitude, distanceToTarget);
    }
}
