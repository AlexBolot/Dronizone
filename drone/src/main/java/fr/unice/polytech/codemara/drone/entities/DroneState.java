package fr.unice.polytech.codemara.drone.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DroneState {
    private double battery_level;
    private Whereabouts whereabouts;
    private long droneID;
    private DroneStatus droneStatus;
    private long timestamp;
}

