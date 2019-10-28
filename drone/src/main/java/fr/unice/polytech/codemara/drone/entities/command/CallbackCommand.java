package fr.unice.polytech.codemara.drone.entities.command;

import fr.unice.polytech.codemara.drone.entities.Location;
import lombok.Data;

@Data
public class CallbackCommand extends DroneCommand {

    private final Location baseLocation;

    public CallbackCommand(CommandType type, Location baseLocation) {
        super(type);
        this.baseLocation = baseLocation;
        this.setPayload(baseLocation);
    }

}
