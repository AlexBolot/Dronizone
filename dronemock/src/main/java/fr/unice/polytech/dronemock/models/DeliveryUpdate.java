package fr.unice.polytech.dronemock.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryUpdate {

    private long droneId;

    private long orderId;

    private long itemId;

    private DeliveryStatus deliveryStatus;

}
