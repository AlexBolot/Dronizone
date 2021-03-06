package fr.unice.polytech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.entities.Customer;
import fr.unice.polytech.entities.Order;
import fr.unice.polytech.entities.OrderStatusMessage;
import fr.unice.polytech.entities.Status;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gherkin.deps.com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/order/notify", produces = "application/json")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final String NOTIFY_URL = "http://localhost:8080";
    private static final String NOTIFY_PATH = "/notification/customer/";

    private final KafkaTemplate kafkaTemplate;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private Environment env;

    public OrderController(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/{hello}")
    public String order_ping(@PathVariable("hello") String hello) {
        return hello.equals("hello") ? "World" : "Hello";
    }

    @GetMapping("/delivery/{order_id}")
    public String notifyDelivery(@PathVariable("order_id") int orderId) {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (!opt.isPresent()) return "KO";

        Order order = opt.get();
        Customer customer = order.getCustomer();

        Map<String, String> params = new HashMap<>();
        params.put("customer_name", customer.getName() + " " + customer.getFirstName());
        params.put("medium", customer.getMedium().name());
        params.put("item_name", order.getItem().getName());
        params.put("payload", "Your delivery will arrive in 10 minutes");

        String notifyUrl = env.getProperty("NOTIFY_HOST");
        if (notifyUrl == null) notifyUrl = NOTIFY_URL;


        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(notifyUrl + NOTIFY_PATH + customer.getId() + "/order", params, String.class);

        return "OK";
    }

    @GetMapping("/cancel/{order_id}")
    public String notifyCancel(@PathVariable("order_id") int orderId) {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (!opt.isPresent()) return "KO";

        Order order = opt.get();
        Customer customer = order.getCustomer();

        Map<String, String> params = new HashMap<>();
        params.put("customer_name", customer.getName() + " " + customer.getFirstName());
        params.put("medium", customer.getMedium().name());
        params.put("item_name", order.getItem().getName());
        params.put("payload", "Your delivery is cancel");

        String notifyUrl = env.getProperty("NOTIFY_HOST");
        if (notifyUrl == null) notifyUrl = NOTIFY_URL;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(notifyUrl + NOTIFY_PATH + customer.getId() + "/order", params, String.class);

        return "OK";
    }

    @KafkaListener(topics = "deliveryPostponed", groupId = "order-service")
    public void listenForPostponed(String content) throws IOException {
        ObjectNode jsonNode = new ObjectMapper().readValue(content, ObjectNode.class);
        int orderId = jsonNode.get("orderId").asInt();
        notifyCancel(orderId);
    }

    @GetMapping("/kafka")
    public void kafkaTest() {
        kafkaTemplate.send("orders", "{\"orderId\":2; \"status\":\"soon\"}");
    }

    /**
     * This struct will maybe change if the data sent here get more complex
     * {"orderId":x; "status":"soon"}
     *
     * @param content
     */

    @KafkaListener(topics = "orders")
    public void checkIfClose(String content) {
        Gson gson = new Gson();
        OrderStatusMessage osm = gson.fromJson(content, OrderStatusMessage.class);
        int orderId = osm.getOrder_id();
        String status = osm.getStatus();
        Optional<Order> opt = orderRepo.findById(orderId);
        if (!opt.isPresent()) {
            System.err.println("err");
        } else {
            if (status.equals("soon")) {
                notifyDelivery(orderId);
                Order order = opt.get();
                order.setStatus(Status.SOON);
            }
        }
    }
}
