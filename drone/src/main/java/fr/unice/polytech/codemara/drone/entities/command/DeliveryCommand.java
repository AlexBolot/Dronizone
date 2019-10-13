package fr.unice.polytech.codemara.drone.entities.command;

import fr.unice.polytech.codemara.drone.entities.Delivery;
import lombok.Data;

import static fr.unice.polytech.codemara.drone.entities.command.CommandType.DELIVERY;

@Data
public class DeliveryCommand extends DroneCommand {
    public DeliveryCommand() {
        super(DELIVERY);
    }

    private Delivery delivery;
}
