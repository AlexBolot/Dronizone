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
    private long itemId;
    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude_pickup")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude_pickup"))
    })
    private Location pickup_location;
    @Embedded
    private Location target_location;
    
}
