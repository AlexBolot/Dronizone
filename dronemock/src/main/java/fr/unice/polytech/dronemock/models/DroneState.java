package fr.unice.polytech.dronemock.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DroneState {
    private double battery;
    private Whereabouts whereabouts;
    private long drone_id;
    private DroneStatus droneStatus;
    private long timestamp;
}

