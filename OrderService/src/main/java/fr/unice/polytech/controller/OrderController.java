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
    private static final String NOTIFY_PATH = "/notifications/customer/";

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private Environment env;

    @GetMapping("/**")
    public String debug() {
        return "OK";
    }

    @KafkaListener(topics = "order-delivered", groupId = "order-service")
    public void listenForDelivered(String content) throws IOException {
        ObjectNode jsonNode = new ObjectMapper().readValue(content, ObjectNode.class);
        int orderId = jsonNode.get("orderId").asInt();
        sendNotification("Your delivery has been delivered", orderId);
    }

    @KafkaListener(topics = "order-soon", groupId = "order-service")
    public void listenForSoon(String content) throws IOException {
        ObjectNode jsonNode = new ObjectMapper().readValue(content, ObjectNode.class);
        int orderId = jsonNode.get("orderId").asInt();
        sendNotification("Your delivery will arrive in 10 minutes", orderId);
    }

    @KafkaListener(topics = "order-cancelled", groupId = "order-service")
    public void listenForCancelled(String content) throws IOException {
        ObjectNode jsonNode = new ObjectMapper().readValue(content, ObjectNode.class);
        int orderId = jsonNode.get("orderId").asInt();
        sendNotification("Your delivery is cancelled", orderId);
    }

    private void sendNotification(String payload, int orderId) {
        Optional<Order> opt = orderRepo.findById(orderId);
        if (opt.isPresent()) {
            Order order = opt.get();
            Customer customer = order.getCustomer();

            Map<String, String> params = new HashMap<>();
            params.put("customer_name", customer.getName() + " " + customer.getFirstName());
            params.put("medium", customer.getMedium().name());
            params.put("item_name", order.getItem().getName());
            params.put("payload", payload);

            String notifyUrl = env.getProperty("NOTIFY_HOST");
            if (notifyUrl == null) notifyUrl = NOTIFY_URL;

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(notifyUrl + NOTIFY_PATH + customer.getId() + "/order", params, String.class);
        }
    }
}
