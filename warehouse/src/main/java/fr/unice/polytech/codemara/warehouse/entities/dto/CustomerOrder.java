package fr.unice.polytech.codemara.warehouse.entities.dto;

import fr.unice.polytech.codemara.warehouse.entities.Location;
import lombok.*;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOrder {

    int orderId;

    int itemId;

    int customerId;

    Location deliveryLocation;

    long timestamp;
}
