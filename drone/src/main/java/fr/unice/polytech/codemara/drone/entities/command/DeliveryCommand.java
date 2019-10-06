package fr.unice.polytech.codemara.drone.entities.command;


import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static fr.unice.polytech.codemara.drone.entities.command.CommandType.DELIVERY;
@Data
public class DeliveryCommand extends DroneCommand {
    public DeliveryCommand() {
        super(DELIVERY);
    }
    private Delivery delivery;
}
