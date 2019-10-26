package fr.unice.polytech.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import fr.unice.polytech.entities.*;
import fr.unice.polytech.repo.CoordRepo;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import fr.unice.polytech.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AutoJsonRpcServiceImpl
public class OrderServiceImpl implements OrderService {

    private static final String WAREHOUSE_PATH = "/warehouse/orders";
    private static final String WAREHOUSE_URL = "http://localhost:8080";

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CoordRepo coordRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Override
    public Order orderItem(Order order) {
        order.setStatus(Status.PENDING);

        itemRepo.save(order.getItem());
        customerRepo.save(order.getCustomer());
        coordRepo.save(order.getCoord());
        orderRepo.save(order);

        String warehouseUrl = env.getProperty("WAREHOUSE_HOST");
        if (warehouseUrl == null) warehouseUrl = WAREHOUSE_URL;


        Map<String, Object> params = new HashMap<>();
        params.put("orderId", order.getId().toString());
        params.put("itemId", order.getItem().getId().toString());
        params.put("customerId", order.getCustomer().getId().toString());

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", order.getCoord().getLat());
        location.put("longitude", order.getCoord().getLon());

        params.put("deliveryLocation", location);

        try {
            kafkaTemplate.send("order-create", new ObjectMapper().writeValueAsString(params));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return order;
    }

    @Override
    public NotificationMedium setPersonalPreferences(int customerId, NotificationMedium medium) {
        Optional<Customer> optCustomer = customerRepo.findById(customerId);

        if (!optCustomer.isPresent())
            throw new IllegalArgumentException("No Customer found with id " + customerId);

        Customer customer = optCustomer.get();
        customer.setMedium(medium);
        customerRepo.save(customer);

        return customer.getMedium();
    }

    @Override
    public int registerCustomer(String name, String firstName) {
        Customer customer = new Customer(name, firstName);
        customerRepo.save(customer);
        return customer.getId();
    }
}
