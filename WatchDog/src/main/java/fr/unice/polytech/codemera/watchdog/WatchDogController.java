package fr.unice.polytech.codemera.watchdog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.codemera.watchdog.entities.Order;
import fr.unice.polytech.codemera.watchdog.entities.Order.OrderStatus;
import fr.unice.polytech.codemera.watchdog.repositories.OrderRepository;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDateTime.now;

@RestController
@RequestMapping(path = "/watchdog", produces = "application/json")
public class WatchDogController {

    private static final String NOTIFY_URL = "http://localhost:8080";
    private static final String NOTIFY_PATH = "/notification/alert/";

    private OrderRepository orderRepository;
    private Environment env;

    public WatchDogController(OrderRepository orderRepository, Environment env) {
        this.orderRepository = orderRepository;
        this.env = env;
    }

    @KafkaListener(topics = "orders")
    public void order_listener(String message) throws IOException {

        ObjectNode json = new ObjectMapper().readValue(message, ObjectNode.class);

        int order_id = json.get("order_id").asInt();
        OrderStatus status = OrderStatus.valueOf(json.get("orderStatus").asText());
        JsonNode payload = json.get("payload");

        int customer_id = payload.get("customer_id").asInt();

        Order order = new Order(order_id, customer_id, status, System.currentTimeMillis());
        handleNewOrder(order);
    }


    private void handleNewOrder(Order order) {
        Iterator<Order> orders = orderRepository.findAllByCustomer_id(order.getCustomer_id()).iterator();

        List<Order> recent = new ArrayList<>();
        List<Order> old = new ArrayList<>();

        while (orders.hasNext()) {
            Order next = orders.next();
            LocalDateTime time = new Timestamp(next.getTimestamp()).toLocalDateTime();

            if (time.isBefore(now().minusMinutes(1))) old.add(next);
            else recent.add(next);

            if (old.size() >= 20) {
                Map<String, String> params = new HashMap<>();
                params.put("target_id", String.valueOf(order.getCustomer_id()));
                params.put("order_id", String.valueOf(order.getOrder_id()));
                params.put("payload", "I detected a strange customer behavior !!");

                String notifyUrl = env.getProperty("NOTIFY_HOST");
                if (notifyUrl == null) notifyUrl = NOTIFY_URL;

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.postForObject(notifyUrl + NOTIFY_PATH, params, String.class);
            }

            recent.forEach(orderRepository::delete);
        }
    }
}
