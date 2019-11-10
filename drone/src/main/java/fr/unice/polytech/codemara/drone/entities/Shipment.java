package fr.unice.polytech.codemara.drone.entities;

import fr.unice.polytech.codemara.drone.entities.dto.ShipmentDTO;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.dto.DeliveryStatus.DELIVERED;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude_pickup")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude_pickup"))
    })
    private Location pickupLocation;

    @ElementCollection
    private List<Delivery> deliveries;

    private double shipmentWeight;

    private long timestamp;

    public Shipment(ShipmentDTO dto) {
        pickupLocation = dto.getPickupLocation();
        shipmentWeight = dto.getShipmentWeight();
        timestamp = dto.getTimestamp();
        deliveries = dto.getOrders().stream().map(Delivery::new).collect(Collectors.toList());
    }

    public boolean hasNext() {
        return deliveries.stream().anyMatch(delivery -> delivery.getStatus() == DELIVERED);
    }

    public Delivery next() {
        Optional<Delivery> optDelivery = deliveries.stream().filter(delivery -> delivery.getStatus() != DELIVERED).findFirst();
        return optDelivery.orElse(null);
    }
}
