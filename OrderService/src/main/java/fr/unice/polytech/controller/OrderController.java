package fr.unice.polytech.controller;

import fr.unice.polytech.entities.Customer;
import fr.unice.polytech.entities.Item;
import fr.unice.polytech.entities.Order;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/order", produces = "application/json")
public class OrderController {

    private static final String WAREHOUSE_URL = "http://localhost:8080/notify";

    @Autowired
    private ItemRepo itemRepo;

    @GetMapping("/{hello}")
    public String order_ping(@PathVariable("hello") String hello){
        if (hello.equals("hello")){
            return "World";

        }
        return "Hello";
    }

    @GetMapping("/ping/{customer_id}/{item_id}")
    public String orderPing(@PathVariable("customer_id") int clientId, @PathVariable("item_id") int itemId) {
        Item item = itemRepo.findItemById(itemId);

        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"customer_id\":\"").append(clientId).append("\",");
        stringBuilder.append("\"item_name\":\"").append(item.getName()).append("\",");
        stringBuilder.append("\"payload\":\"Your delivery will arrived in 10 minutes\"}");

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(WAREHOUSE_URL).openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");

            try (OutputStream out = conn.getOutputStream()) {
                out.write(stringBuilder.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Ok";
    }

//    @RequestMapping(method = POST, path = "/newOrder")
//    public Order greeting(@RequestBody String address,
//                          @RequestBody String item) {
//        return new Order(address, item);
//    }
}
