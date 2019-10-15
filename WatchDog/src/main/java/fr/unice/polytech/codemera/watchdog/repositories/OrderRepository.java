package fr.unice.polytech.codemera.watchdog.repositories;

import fr.unice.polytech.codemera.watchdog.entities.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {
    public Order findByCustomer_id(long o, long Item_id);
}
