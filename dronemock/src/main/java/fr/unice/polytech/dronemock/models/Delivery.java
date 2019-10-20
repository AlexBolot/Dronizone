package fr.unice.polytech.dronemock.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Delivery {

    private long id;
    private long orderId;
    private long itemId;

    private Location pickup_location;

    private Location target_location;
    private boolean notified = false;


}
