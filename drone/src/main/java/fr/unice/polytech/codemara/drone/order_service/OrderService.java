package fr.unice.polytech.codemara.drone.order_service;

import fr.unice.polytech.codemara.drone.entities.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class OrderService {

    private Environment env;
    private final KafkaTemplate kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(Environment env, KafkaTemplate kafkaTemplate) {
        this.env = env;
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
