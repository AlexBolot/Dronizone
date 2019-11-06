package fr.unice.polytech.codemara.warehouse.entities.dto;

import fr.unice.polytech.codemara.warehouse.entities.Location;
import lombok.*;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PackedOrder {

    int orderId;
    String status;
    int customerId;
    Location deliveryLocation;
    Location pickupLocation;
    long timestamp;
}
