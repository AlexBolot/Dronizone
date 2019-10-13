package fr.unice.polytech.codemara.drone.entities.dto;

import fr.unice.polytech.codemara.drone.entities.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
