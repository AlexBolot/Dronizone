package fr.unice.polytech.codemara.drone.entities.command;

import fr.unice.polytech.codemara.drone.entities.Location;

import java.util.Objects;


public class CallbackCommand extends DroneCommand {

    private final Location baseLocation;

    @Override
    public Object getPayload() {
        return this.baseLocation;
    }

    public CallbackCommand(Location baseLocation) {
        super(CommandType.CALLBACK);
        this.baseLocation = baseLocation;
        this.setPayload(baseLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CallbackCommand that = (CallbackCommand) o;
        return Objects.equals(baseLocation, that.baseLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseLocation);
    }
}
