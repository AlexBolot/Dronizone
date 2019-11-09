package fr.unice.polytech.codemara.warehouse.entities.dto;

import fr.unice.polytech.codemara.warehouse.entities.Location;
import lombok.*;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOrder {

    private int orderId;

    private int itemId;

    private int customerId;

    private Location deliveryLocation;

    private double weight;

    private long timestamp;
}
