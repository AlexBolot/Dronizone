package fr.unice.polytech.codemara.drone.entities.dto;

import fr.unice.polytech.codemara.drone.entities.Location;
import fr.unice.polytech.codemara.drone.entities.Shipment;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDTO {
    private Location pickupLocation;
    private List<DeliveryDTO> orders;
    private double shipmentWeight;
    private long timestamp;

    public ShipmentDTO(Shipment shipment) {
        pickupLocation = shipment.getPickupLocation();
        orders = shipment.getDeliveries().stream().map(DeliveryDTO::new).collect(Collectors.toList());
        shipmentWeight = shipment.getShipmentWeight();
        timestamp = shipment.getTimestamp();
    }
}
