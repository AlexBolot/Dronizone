package fr.unice.polytech.controller;

import fr.unice.polytech.entities.Customer;
import fr.unice.polytech.entities.Item;
import fr.unice.polytech.entities.Order;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
    public String order_ping(@PathVariable("hello") String hello){
        if (hello.equals("hello")){
            return "World";

        }
        return "Hello";
    }

    @GetMapping("/delivery/{order_id}")
    public String notifyDelivery(@PathVariable("order_id") int orderId) {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (!opt.isPresent()) return "KO";

        Order order = opt.get();

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", order.getCustomer().getId() + "");
        params.put("item_name", order.getItem().getName());
        params.put("payload", "Your delivery will arrived in 10 minutes");

        String notifyUrl = env.getProperty("NOTIFY_HOST");
        if (notifyUrl == null) notifyUrl = NOTIFY_URL;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(notifyUrl + NOTIFY_PATH + order.getId() + "/order", params, String.class);

        return "OK";
    }
    @GetMapping("/cancel/{order_id}")
    public String notifyCancel(@PathVariable("order_id") int orderId) {
        Optional<Order> opt = orderRepo.findById(orderId);

        if (!opt.isPresent()) return "KO";

        Order order = opt.get();

        Map<String, String> params = new HashMap<>();
        params.put("customer_id", order.getCustomer().getId() + "");
        params.put("item_name", order.getItem().getName());
        params.put("payload", "Your delivery is cancel");

        String notifyUrl = env.getProperty("NOTIFY_HOST");
        if (notifyUrl == null) notifyUrl = NOTIFY_URL;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(notifyUrl + NOTIFY_PATH + order.getId() + "/order", params, String.class);

        return "OK";
    }

//    @RequestMapping(method = POST, path = "/newOrder")
//    public Order greeting(@RequestBody String address,
//                          @RequestBody String item) {
//        return new Order(address, item);
//    }
}
