package fr.unice.polytech;

import fr.unice.polytech.entities.*;
import fr.unice.polytech.repo.OrderRepo;
import fr.unice.polytech.service.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepo orderRepo;

    private Item item;
    private Customer customer;

    @Before
    public void setup() {
        item = new Item("Persona 5");
        customer = new Customer("Roger", "Regor");
    }

    @Test
    public void orderItemTest() {
        Order order = new Order(new Coord("0", "0"), item, null, customer, "bla bla");
        Order received = orderService.orderItem(order);
        assertNotEquals(order, received);
        Optional<Order> opt = orderRepo.findById(received.getId());
        assertTrue(opt.isPresent());
        assertEquals(received, opt.get());
    }

}
