package fr.unice.polytech.codemara.drone.entities.command;

import fr.unice.polytech.codemara.drone.entities.Drone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Represent an order to be sent to a drone
 * Is a prototype pattern implementation, can be copied with variation
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor

public class DroneCommand {
    private final CommandType type;
    private Drone target;

    /**
     * constructor for child purposes, should not be used
     */
    private DroneCommand() {
        type = null;
    }

    public DroneCommand copyWith(Drone target){
        DroneCommand copy = new DroneCommand(this.type);
        copy.target = target;
        return copy;
    }
}
