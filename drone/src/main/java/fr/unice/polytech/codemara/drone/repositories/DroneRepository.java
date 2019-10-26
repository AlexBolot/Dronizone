package fr.unice.polytech.codemara.drone.repositories;



import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.DroneStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DroneRepository extends CrudRepository<Drone,Long> {
    Iterable<Drone> getDronesByDroneStatus(DroneStatus droneStatus);
    Iterable<Drone> getDroneByCurrentDelivery(Delivery delivery);
}
