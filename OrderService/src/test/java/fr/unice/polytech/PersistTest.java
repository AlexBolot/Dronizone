package fr.unice.polytech;

import fr.unice.polytech.entities.*;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PersistTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Test
    public void customerTest() {
        Customer customer = new Customer("Doe", "John");
        customerRepo.save(customer);

        Optional<Customer> optCustomer = customerRepo.findById(customer.getId());
        assertTrue(optCustomer.isPresent());
        assertEquals(customer, optCustomer.get());
    }

    @Test
    public void itemTest() {
        Item item = new Item("Persona 5", 5.5);
        itemRepo.save(item);
        assertEquals(item, itemRepo.findItemById(item.getId()));
    }

    @Test
    public void orderTest() {
        Item item = new Item("Persona 5", 5.5);
        Customer customer = new Customer("Doe", "John");
        Order order = new Order(new Coord("0", "0"), item, Status.PENDING, customer, "bla bla");
        orderRepo.save(order);
        Optional<Order> opt = orderRepo.findById(order.getId());
        assertTrue(opt.isPresent());
        assertEquals(order, opt.get());
    }
}
