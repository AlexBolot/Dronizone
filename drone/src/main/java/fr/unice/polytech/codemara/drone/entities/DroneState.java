package fr.unice.polytech.codemara.drone.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DroneState {
    private int battery;
    private Whereabouts whereabouts;
    private long drone_id;
    private DroneStatus droneStatus;
    private long timestamp;
}

