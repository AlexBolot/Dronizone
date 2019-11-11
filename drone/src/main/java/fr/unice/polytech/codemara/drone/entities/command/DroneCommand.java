package fr.unice.polytech.codemara.drone.entities.command;

import fr.unice.polytech.codemara.drone.entities.Drone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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

    private Object payload;


    public long getDroneId() {
        return target.getDroneID();
    }

    public DroneCommand copyWith(Drone target) {
        DroneCommand copy = new DroneCommand(this.type);
        copy.target = target;
        copy.payload = payload;
        return copy;
    }
}
