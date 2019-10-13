package fr.unice.polytech.controller;

import fr.unice.polytech.entities.Order;
import fr.unice.polytech.entities.OrderStatusMessage;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import gherkin.deps.com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/order/notify", produces = "application/json")
public class OrderController {

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
        if (hello.equals("hello")) {
            return "World";

        }
        return "Hello";
    }

    @GetMapping("/delivery/{order_id}")
    public String notifyDelivery(@PathVariable("order_id") int orderId) {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (!opt.isPresent()) return "KO";

        Order order = opt.get();

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", order.getCustomer().getId() + "");
        params.put("item_name", order.getItem().getName());
        params.put("payload", "Your delivery will arrive in 10 minutes");

        String notifyUrl = env.getProperty("NOTIFY_HOST");
        if (notifyUrl == null) notifyUrl = NOTIFY_URL;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(notifyUrl + NOTIFY_PATH + order.getCustomer().getId() + "/order", params, String.class);

        return "OK";
    }

    @GetMapping("/cancel/{order_id}")
    public String notifyCancel(@PathVariable("order_id") int orderId) {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (!opt.isPresent()) return "KO";

        Order order = opt.get();

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", order.getCustomer().getId() + "");
        params.put("item_name", order.getItem().getName());
        params.put("payload", "Your delivery is cancel");

        String notifyUrl = env.getProperty("NOTIFY_HOST");
        if (notifyUrl == null) notifyUrl = NOTIFY_URL;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(notifyUrl + NOTIFY_PATH + order.getCustomer().getId() + "/order", params, String.class);

        return "OK";
    }

    @GetMapping("/kafka")
    public void kafkaTest() {
        kafkaTemplate.send("delivery", "{\"orderId\":2; \"status\":\"soon\"}");
    }

    /**
     * This struct will maybe change if the data sent here get more complex
     * {"orderId":x; "status":"soon"}
     *
     * @param content
     */

    @KafkaListener(topics = "delivery")
    public void checkIfClose(String content) {
        Gson gson = new Gson();
        OrderStatusMessage osm = gson.fromJson(content, OrderStatusMessage.class);
        int orderId = osm.getOrder_id();
        String status = osm.getStatus();
        if (status.equals("soon")) {
            notifyDelivery(orderId);
        }
    }
}
