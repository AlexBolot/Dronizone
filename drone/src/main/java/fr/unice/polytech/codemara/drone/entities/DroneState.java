package fr.unice.polytech.codemara.drone.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DroneState {
    public int battery;
    public Whereabouts whereabouts;
    public long drone_id;
    public DroneStatus droneStatus;
}

