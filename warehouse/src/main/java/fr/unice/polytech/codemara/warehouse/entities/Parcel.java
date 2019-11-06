package fr.unice.polytech.codemara.warehouse.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int orderId;

    private int itemId;

    private int customerId;

    @Embedded
    private Location deliveryLocation;

    private ParcelStatus status;

}
