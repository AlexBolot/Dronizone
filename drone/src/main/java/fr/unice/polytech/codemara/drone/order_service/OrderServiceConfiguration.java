package fr.unice.polytech.codemara.drone.order_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class OrderServiceConfiguration {
    @Bean
    public OrderService orderService(KafkaTemplate kafkaTemplate) {
        return new OrderService(kafkaTemplate);
    }
}
