package fr.unice.polytech.codemara.drone.repositories;



import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.DroneStatus;
import org.springframework.data.repository.CrudRepository;

public interface DroneRepository extends CrudRepository<Drone,Long> {
    public Iterable<Drone> getDronesByDroneStatus(DroneStatus droneStatus);
    public Iterable<Drone> getDroneByCurrentDelivery(Delivery delivery);
}
