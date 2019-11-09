package fr.unice.polytech.codemara.warehouse.entities.dto;

import fr.unice.polytech.codemara.warehouse.entities.Location;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PackedShipment {

    private Location pickupLocation;

    private List<PackedOrder> orders;

    private double shipmentWeight;

    private long timestamp;

}
