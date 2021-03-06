package fr.unice.polytech.codemara.drone.repositories;

import fr.unice.polytech.codemara.drone.entities.Delivery;
import org.springframework.data.repository.CrudRepository;

public interface DeliveryRepository extends CrudRepository<Delivery,Long> {
    public Delivery findByOrderIdAndItemId(long o,long Item_id);
}
