package fr.unice.polytech.codemara.warehouse.entities;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int shipmentId;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Parcel> parcels;

    @Embedded
    private Location pickUpLocation;

    public Shipment(Location pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
        this.parcels = new ArrayList<>();
    }

}
