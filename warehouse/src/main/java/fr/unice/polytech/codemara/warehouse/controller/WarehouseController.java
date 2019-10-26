package fr.unice.polytech.codemara.warehouse.controller;

import fr.unice.polytech.codemara.warehouse.entities.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.repositories.OrderRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/warehouse", produces = "application/json")
public class WarehouseController {

    private final Environment env;
    private final String warehouse_lon = "10.0";
    private final String warehouse_lat = "10.0";
    final OrderRepository orderRepository;

    public WarehouseController(Environment env, OrderRepository orderRepository) {
        this.env = env;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/orders")
    public Iterable<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping("/orders")
    public String addOrder(@RequestBody CustomerOrder order) {
        return orderRepository.save(order).toString();
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable("id") int id) {
        return orderRepository.findById(id).toString();
    }

    @PutMapping("/orders/{id}")
    public String orderReady(@PathVariable("id") int id) {
        Optional<CustomerOrder> ready = orderRepository.findById(id);
        if (ready.isPresent()) {
            ready.get().setStatus(CustomerOrder.OrderStatus.READY);
            orderRepository.save(ready.get());
        }
        try {
            URL url = new URL(env.getProperty("DRONE_HOST")+"/drone/request_delivery");
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("orderid", "id");
            Map<String,String> position = new HashMap<>();
            position.put("latitude",this.warehouse_lat);
            position.put("longitude",this.warehouse_lon);
            parameters.put("pickup_location",position);
            position = new HashMap<>();
            position.put("latitude",ready.get().getLat());
            position.put("longitude",ready.get().getLon());
            parameters.put("target_location",position);
            parameters.put("itemId",ready.get().getItem_id());
            ResponseEntity<String> response
                    = restTemplate.postForEntity(url.toString(),parameters, String.class);
            System.out.println("response = " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Message envoyé à un autre service";
    }


}