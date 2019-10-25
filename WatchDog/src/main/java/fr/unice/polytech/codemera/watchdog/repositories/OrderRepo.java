package fr.unice.polytech.codemera.watchdog.repositories;

import fr.unice.polytech.codemera.watchdog.entities.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepo extends CrudRepository<Order, Long> {
    public long countAllByCustomerIdAndTimestampIsAfter(int customer_id, long timestamp);
    public Iterable<Order> findAllByCustomerIdAndTimestampIsBefore(int customer_id, long timestamp);
}
