package fr.unice.polytech.codemara.drone.entities.dto;

import fr.unice.polytech.codemara.drone.entities.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {
    private long orderId;
    private String status;
    private Location pickupLocation;
    private Location targetLocation;
    private long timestamps;
}
