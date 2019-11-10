package fr.unice.polytech.codemara.drone.entities.dto;

import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Location;
import lombok.*;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDTO {
    private long orderId;
    private String status;
    private Location deliveryLocation;

    public DeliveryDTO(Delivery delivery) {
        orderId = delivery.getOrderId();
        status = delivery.getStatus().name();
        deliveryLocation = delivery.getDeliveryLocation();
    }
}
