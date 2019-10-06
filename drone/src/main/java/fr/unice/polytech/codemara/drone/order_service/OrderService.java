package fr.unice.polytech.codemara.drone.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

public class OrderService {

    private Environment env;

    public OrderService(Environment env) {
        this.env = env;
    }
    public void cancel(Delivery delivery){
        try {
            URL url = UriComponentsBuilder.fromUriString(env.getProperty("ORDER_SERVICE_HOST")+"/order/notify/cancel/"+delivery.getOrderId())
                    .build().toUri().toURL();
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(url.toString(), String.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
}
