package fr.unice.polytech.repo;

import fr.unice.polytech.entities.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerRepo extends CrudRepository<Customer, Integer> {

    default Customer updateFrom(Customer customer) {
        Optional<Customer> optCustomer = findById(customer.getId());

        if (!optCustomer.isPresent())
            throw new IllegalArgumentException("No customer found with id " + customer.getId());

        return optCustomer.get();
    }

}
