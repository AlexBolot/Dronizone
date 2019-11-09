package fr.unice.polytech.codemara.warehouse.repositories;

import fr.unice.polytech.codemara.warehouse.entities.Shipment;
import org.springframework.data.repository.CrudRepository;

public interface ShipmentRepository extends CrudRepository<Shipment, Integer> {
}
