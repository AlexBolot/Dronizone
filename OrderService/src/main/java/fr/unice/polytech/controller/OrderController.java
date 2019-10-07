package fr.unice.polytech.controller;

import fr.unice.polytech.entities.Order;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/order/notify", produces = "application/json")
public class OrderController {

    private static final String NOTIFY_URL = "http://localhost:8080";
    private static final String NOTIFY_PATH = "/notification/customer/";

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private Environment env;

    @GetMapping("/{hello}")
    public String order_ping(@PathVariable("hello") String hello) {
        return hello.equals("hello") ? "World" : "Hello";
    }

    @GetMapping("/delivery/{order_id}")
    public String notifyDelivery(@PathVariable("order_id") int orderId) {
        return notifyStatusChanged(orderId, "Your delivery will arrive in 10 minutes");
    }

    @GetMapping("/cancel/{order_id}")
    public String notifyCancel(@PathVariable("order_id") int orderId) {
        return notifyStatusChanged(orderId, "Your delivery is cancel");
    }

    private String notifyStatusChanged(int orderId, String message) {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (!opt.isPresent()) return "KO";

        Order order = opt.get();

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", order.getCustomer().getId() + "");
        params.put("item_name", order.getItem().getName());
        params.put("payload", message);

        String notifyUrl = env.getProperty("NOTIFY_HOST");
        if (notifyUrl == null) notifyUrl = NOTIFY_URL;

        new RestTemplate().postForObject(notifyUrl + NOTIFY_PATH + order.getCustomer().getId() + "/order", params, String.class);

        return "OK";
    }
}
