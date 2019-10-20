package fr.unice.polytech.codemara.drone.entities.command;


import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static fr.unice.polytech.codemara.drone.entities.command.CommandType.DELIVERY;

@Data
public class DeliveryCommand extends DroneCommand {

    private final Delivery delivery;

    public DeliveryCommand(Drone target, Delivery delivery) {
        super(DELIVERY, target);
        this.delivery = delivery;
    }


}
