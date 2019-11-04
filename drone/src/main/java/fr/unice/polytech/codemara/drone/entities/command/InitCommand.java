package fr.unice.polytech.codemara.drone.entities.command;

import fr.unice.polytech.codemara.drone.entities.Drone;
import lombok.Data;

@Data
public class InitCommand extends DroneCommand {

    public final long assignedId;

    public InitCommand(Drone target, long newId) {
        super(CommandType.INITIALISATION, target, newId);
        this.assignedId = newId;
    }
}
