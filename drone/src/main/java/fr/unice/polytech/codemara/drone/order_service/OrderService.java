package fr.unice.polytech.codemara.drone.order_service;

import fr.unice.polytech.codemara.drone.entities.Delivery;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class OrderService {

    private Environment env;
    private final KafkaTemplate kafkaTemplate;

    public OrderService(Environment env, KafkaTemplate kafkaTemplate) {
        this.env = env;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void cancel(Delivery delivery) {
        try {
            URL url = UriComponentsBuilder.fromUriString(env.getProperty("ORDER_SERVICE_HOST") + "/order/notify/cancel/" + delivery.getOrderId())
                    .build().toUri().toURL();
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(url.toString(), String.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

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
