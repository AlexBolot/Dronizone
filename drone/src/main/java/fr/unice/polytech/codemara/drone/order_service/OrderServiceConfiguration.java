package fr.unice.polytech.codemara.drone.order_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class OrderServiceConfiguration {
    @Bean
    public OrderService orderService(Environment env){
        return new OrderService(env);
    }
}
