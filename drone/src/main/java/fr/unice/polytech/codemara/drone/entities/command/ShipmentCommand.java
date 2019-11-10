package fr.unice.polytech.codemara.drone.entities.command;

import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.Shipment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static fr.unice.polytech.codemara.drone.entities.command.CommandType.DELIVERY;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShipmentCommand extends DroneCommand {
    private final Shipment shipment;

    public ShipmentCommand(Drone target, Shipment shipment) {
        super(DELIVERY, target, shipment);
        this.shipment = shipment;
    }
}
