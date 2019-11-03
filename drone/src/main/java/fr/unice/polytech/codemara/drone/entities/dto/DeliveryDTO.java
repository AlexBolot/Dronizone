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
    private long itemId;
    private Location pickup_location;
    private Location target_location;
    private boolean notified = false;
}
