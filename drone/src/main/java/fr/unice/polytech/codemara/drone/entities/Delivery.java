package fr.unice.polytech.codemara.drone.entities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue
    private long id;
    private long orderId;
    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude_pickup")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude_pickup"))
    })
    private Location pickup_location;
    @Embedded
    private Location target_location;
    private boolean notified = false;
    private boolean picked_up = false;

    public boolean mustNotify() {
        return !notified && picked_up;
    }
}