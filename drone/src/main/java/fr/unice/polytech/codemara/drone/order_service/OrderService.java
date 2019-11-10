package fr.unice.polytech.codemara.drone.order_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.dto.OrderStatus;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

import static fr.unice.polytech.codemara.drone.entities.dto.OrderStatus.*;

public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void notifyDeliveryCancel(Delivery delivery) {
        logger.info("Delivery was cancel : {}", delivery);
        sendNotification(delivery, "order-cancelled", CANCEL);
    }

    public void notifyDeliverySoon(Delivery delivery) {
        logger.info("Delivery will arrived soon : {}", delivery);
        sendNotification(delivery, "order-soon", SOON);
    }

    public void notifyDeliveryFinish(Delivery delivery) {
        logger.info("Delivery is finish : {}", delivery);
        sendNotification(delivery, "order-delivered", DELIVERED);
    }

    private void sendNotification(Delivery delivery, String topic, OrderStatus status) {
        try {
            long orderId = delivery.getOrderId();
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", orderId);
            params.put("deliveryLocation", delivery.getDeliveryLocation());
            params.put("status", status.toString());
            params.put("timestamp", System.currentTimeMillis());
            kafkaTemplate.send(topic, new ObjectMapper().writeValueAsString(params));
        } catch (JsonProcessingException e) {
            logger.error("OrderService.sendNotification", e);
        }
    }
}
