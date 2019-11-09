package fr.unice.polytech.codemara.warehouse.entities.dto;

import fr.unice.polytech.codemara.warehouse.entities.Location;
import lombok.*;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PackedOrder {

    private int orderId;

    private String status;

    private int customerId;

    private Location deliveryLocation;
}
