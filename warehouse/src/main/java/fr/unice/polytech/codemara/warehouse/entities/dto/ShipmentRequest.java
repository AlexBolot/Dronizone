package fr.unice.polytech.codemara.warehouse.entities.dto;

import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {

    private List<Integer> parcelsId;

}
