package fr.unice.polytech.codemera.watchdog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.codemera.watchdog.entities.Order;
import fr.unice.polytech.codemera.watchdog.entities.Order.OrderStatus;
import fr.unice.polytech.codemera.watchdog.repositories.OrderRepo;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;

@RestController
@RequestMapping(path = "/watchdog", produces = "application/json")
public class WatchDogController {

    private static final String NOTIFY_PATH = "/notification/alert/";

    private OrderRepo orderRepo;
    private Environment env;

    public WatchDogController(OrderRepo orderRepo, Environment env) {
        this.orderRepo = orderRepo;
        this.env = env;
    }

    @KafkaListener(topics = "order-create")
    public void order_listener(String message) throws IOException {

        ObjectNode json = new ObjectMapper().readValue(message, ObjectNode.class);

        int orderId = json.get("order_id").asInt();
        long timestamp = json.get("timestamp").asLong();
        OrderStatus status = OrderStatus.valueOf(json.get("orderStatus").asText());
        JsonNode payload = json.get("payload");

        int customerId = payload.get("customer_id").asInt();

        Order order = new Order(orderId, customerId, status, timestamp);
        handleNewOrder(order);
    }


    private void handleNewOrder(Order order) {
        System.out.println("BLOB####");
        orderRepo.save(order);
        String notifyURL = env.getProperty("NOTIFY_HOST");
        int customerId = order.getCustomerId();

        long thresholdTimestamp = Timestamp.valueOf(now().minusMinutes(1)).getTime();

        long count = orderRepo.countAllByCustomerIdAndTimestampIsAfter(customerId, thresholdTimestamp);

        System.out.println("count ---- "+ count);

        if (count >= 20) {
            Map<String, String> params = new HashMap<>();
            params.put("target_id", String.valueOf(order.getCustomerId()));
            params.put("order_id", String.valueOf(order.getOrderId()));
            params.put("payload", "I detected a strange customer behavior !!");

            String notifyUrl = env.getProperty("NOTIFY_HOST");
            if (notifyUrl == null) notifyUrl = notifyURL;

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(notifyUrl + NOTIFY_PATH, params, String.class);
        }

        Iterable<Order> oldOrders = orderRepo.findAllByCustomerIdAndTimestampIsBefore(customerId, thresholdTimestamp);
        oldOrders.forEach(o -> {
            orderRepo.delete(o);
            System.out.println("[REMOVE] -> " + o);
        });
    }
}
