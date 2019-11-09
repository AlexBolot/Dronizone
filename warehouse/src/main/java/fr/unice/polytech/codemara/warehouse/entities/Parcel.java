//package fr.unice.polytech.codemara.warehouse.entities;
//
//import lombok.*;
//
//import javax.persistence.*;
//
//@Entity
//@Data
//@EqualsAndHashCode
//@ToString
////@AllArgsConstructor
//@NoArgsConstructor
//public class Parcel {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private int orderId;
//
//    private int itemId;
//
//    private int customerId;
//
//    private ParcelStatus status;
//
//    @Embedded
//    private Location deliveryLocation;
//
////    @ManyToOne
////    @JoinColumn(name = "shipmentId")
////    private Shipment shipment;
//
//    public Parcel(int orderId, int itemId, int customerId, ParcelStatus status, Location deliveryLocation) {
//        this.orderId = orderId;
//        this.itemId = itemId;
//        this.customerId = customerId;
//        this.status = status;
//        this.deliveryLocation = deliveryLocation;
////        this.shipment = null;
//    }
//
////    public void setShipment(Shipment shipment) {
////        this.shipment = shipment;
////        if (!shipment.getParcels().contains(this)) {
////            shipment.getParcels().add(this);
////        }
////    }
//}
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
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int orderId;

    private int itemId;

    private int customerId;

    private double weight;

    @Embedded
    private Location deliveryLocation;

    private ParcelStatus status;

}
