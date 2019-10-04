package fr.unice.polytech.service.impl;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import fr.unice.polytech.entities.*;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import fr.unice.polytech.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@AutoJsonRpcServiceImpl
public class OrderServiceImpl implements OrderService {

    private static final String WAREHOUSE_URL = "http://localhost:8080/warehouse";

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Override
    public Order orderItem(int itemId, int customerId, String lon, String lat, String paymentInfo) {
//        itemRepo.save(new Item(0, "Persona 5"));
//        customerRepo.save(new Customer(0, "Gohu", "Francois"));

        Customer c = customerRepo.findCustomerById(customerId);
        Item i = itemRepo.findItemById(itemId);

        Order order = new Order(0, new Coord(lon, lat), i, Status.PENDING, c, paymentInfo);

        orderRepo.save(order);

//        restTemplate.postForObject(WAREHOUSE_URL, order, String.class);

        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"order_id\":\"").append(order.getId()).append("\",");
        stringBuilder.append("\"item_id\":\"").append(itemId).append("\",");
        stringBuilder.append("\"lat\":\"").append(lat).append("\",");
        stringBuilder.append("\"lon\":\"").append(lon).append("\",");
        stringBuilder.append("\"customer_id\":\"").append(customerId).append("\"}");

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
        return order;
    }
}
