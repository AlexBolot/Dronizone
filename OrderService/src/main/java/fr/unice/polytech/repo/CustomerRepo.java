package fr.unice.polytech.repo;

import fr.unice.polytech.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepo extends CrudRepository<Customer, Integer> {

    Customer findCustomerById(Integer id);

}
