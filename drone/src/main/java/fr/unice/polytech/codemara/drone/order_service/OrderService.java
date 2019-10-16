package fr.unice.polytech.codemara.drone.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import static fr.unice.polytech.codemara.drone.entities.OrderStatus.CANCELED;
import static fr.unice.polytech.codemara.drone.entities.OrderStatus.DELIVERY_SOON;

public class OrderService {

    private final KafkaTemplate kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void cancel(Delivery delivery) {
        ObjectNode node = new ObjectMapper().createObjectNode();

        node.put("orderStatus", CANCELED.name());
        node.put("order_id", delivery.getOrderId());

        // Empty payload since no other interesting data required
        node.put("orderPayload", "{}");

        kafkaTemplate.send("orders", node.toString());
    }

    public void notifyDelivery(Delivery delivery) {
     /*   try {
            URL url = UriComponentsBuilder.fromUriString(env.getProperty("ORDER_SERVICE_HOST")+"/order/notify/delivery/"+delivery.getOrderId())
                    .build().toUri().toURL();
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(url.toString(), String.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        long orderId = delivery.getOrderId();
        kafkaTemplate.send("orders", "{\"orderId\":" + orderId + "\"status\":\"soon\"}");
    }
}
