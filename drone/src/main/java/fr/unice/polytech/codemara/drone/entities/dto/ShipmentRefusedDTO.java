package fr.unice.polytech.codemara.drone.entities.dto;

import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Shipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentRefusedDTO {

    private long id;
    private long timestamp;
    private double shipmentWeight;
    private List<Long> deliveryIds;

    public ShipmentRefusedDTO(Shipment shipment) {
        this.id = shipment.getId();
        this.timestamp = shipment.getTimestamp();
        this.shipmentWeight = shipment.getShipmentWeight();
        this.deliveryIds = shipment.getDeliveries().stream().map(Delivery::getId).collect(Collectors.toList());
    }
}
