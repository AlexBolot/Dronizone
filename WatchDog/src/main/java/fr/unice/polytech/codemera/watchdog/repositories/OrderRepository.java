package fr.unice.polytech.codemera.watchdog.repositories;

import fr.unice.polytech.codemera.watchdog.entities.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {
    public Iterable<Order> findAllByCustomer_id(int customer_id);
    public int countAllByCustomer_id(int customer_id);
}
