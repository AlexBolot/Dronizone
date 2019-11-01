package fr.unice.polytech.codemara.drone.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryUpdateDTO {

    private long droneId;
    private long orderId;
    private long itemId;
    //private OrderStatus updateType;
    private DeliveryStatus deliveryStatus;
}
