package fr.unice.polytech.codemara.drone.repositories;

import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.DroneStatus;
import fr.unice.polytech.codemara.drone.entities.Shipment;
import org.springframework.data.repository.CrudRepository;

public interface DroneRepository extends CrudRepository<Drone,Long> {
    Iterable<Drone> getDronesByDroneStatus(DroneStatus droneStatus);
    Iterable<Drone> getDronesByCurrentShipment(Shipment shipment);
    Iterable<Drone> getDronesByCurrentShipmentAndMaxWeightIsGreaterThan(Shipment shipment, double maxWeight);
}
