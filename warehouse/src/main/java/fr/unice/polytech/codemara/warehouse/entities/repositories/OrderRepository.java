package fr.unice.polytech.codemara.warehouse.entities.repositories;

import fr.unice.polytech.codemara.warehouse.entities.CustomerOrder;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<CustomerOrder, Integer> {

}
