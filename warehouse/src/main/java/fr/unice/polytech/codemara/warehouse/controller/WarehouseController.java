package fr.unice.polytech.codemara.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.warehouse.entities.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/warehouse", produces = "application/json")
public class WarehouseController {

    private static final String WAREHOUSE_LON = "10.0";
    private static final String WAREHOUSE_LAT = "10.0";
    private final OrderRepository orderRepository;

    private final KafkaTemplate kafkaTemplate;
    private Logger logger = LoggerFactory.getLogger(WarehouseController.class);

    public WarehouseController(OrderRepository orderRepository, KafkaTemplate kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/orders")
    public Iterable<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping("/orders")
    public String addOrder(@RequestBody String order) throws IOException {
        return orderRepository.save(new ObjectMapper().readValue(order, CustomerOrder.class)).toString();
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable("id") int id) {
        return orderRepository.findById(id).toString();
    }

    @PutMapping("/orders/{id}")
    public String orderReady(@PathVariable("id") int id) {
        Optional<CustomerOrder> ready = orderRepository.findById(id);
        if (ready.isPresent()) {
            ready.get().setStatus(CustomerOrder.OrderStatus.READY);
            orderRepository.save(ready.get());
            try {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("orderid", "id");
                Map<String, String> position = new HashMap<>();
                position.put("latitude", WAREHOUSE_LAT);
                position.put("longitude", WAREHOUSE_LON);
                parameters.put("pickup_location", position);
                position = new HashMap<>();
                position.put("latitude", ready.get().getDeliveryLocation().getLatitude() + "");
                position.put("longitude", ready.get().getDeliveryLocation().getLongitude() + "");
                parameters.put("target_location", position);
                parameters.put("itemId", ready.get().getItemId());

                kafkaTemplate.send("order-packed", new ObjectMapper().writeValueAsString(parameters));
            } catch (Exception e) {
                logger.error("WarehouseController.OrderReady ", e);
            }
        }


        return "Message envoyé à un autre service";
    }

    @KafkaListener(topics = {"order-create"})
    public void orderCreate(String message) {
        try {
            CustomerOrder order = new ObjectMapper().readValue(message, CustomerOrder.class);
            orderRepository.save(order);
        } catch (Exception e) {
            logger.error("WarehouseController.orderCreate", e);
        }
    }


}