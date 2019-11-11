package fr.unice.polytech.codemara.warehouse.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

//@Entity
@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Shipment {

    private List<Parcel> parcels;

    private Location pickUpLocation;

}
