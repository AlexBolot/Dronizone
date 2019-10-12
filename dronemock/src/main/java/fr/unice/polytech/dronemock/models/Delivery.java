package fr.unice.polytech.dronemock.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
public class Delivery {

    private long id;
    private long orderId;
    private long itemId;

    private Location pickup_location;
    @Embedded
    private Location target_location;
    private boolean notified = false;


}
