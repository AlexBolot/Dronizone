package fr.unice.polytech.codemara.warehouse.entities;

import lombok.*;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Parcel {

    @Id
    private int orderId;

    private int itemId;

    @Embedded
    private Location deliveryLocation;

    private ParcelStatus status;

}
