package fr.unice.polytech.service.impl;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import fr.unice.polytech.entities.Customer;
import fr.unice.polytech.entities.NotificationMedium;
import fr.unice.polytech.entities.Order;
import fr.unice.polytech.entities.Status;
import fr.unice.polytech.repo.CoordRepo;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import fr.unice.polytech.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AutoJsonRpcServiceImpl
public class OrderServiceImpl implements OrderService {
    // FIXME: 20/10/2019 Kill it with fire, and replace it with a kafka topic post
    private static final String WAREHOUSE_PATH = "/warehouse/orders";
    private static final String WAREHOUSE_URL = "http://localhost:8080";

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CoordRepo coordRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Override
    public Order orderItem(Order order) {
        order.setStatus(Status.PENDING);

        itemRepo.save(order.getItem());
        order.setCustomer(customerRepo.save(order.getCustomer()));
        coordRepo.save(order.getCoord());
        orderRepo.save(order);

        String warehouseUrl = env.getProperty("WAREHOUSE_HOST");
        if (warehouseUrl == null) warehouseUrl = WAREHOUSE_URL;


        Map<String, String> params = new HashMap<>();
        params.put("order_id", order.getId().toString());
        params.put("item_id", order.getItem().getId().toString());
        params.put("lat", order.getCoord().getLat());
        params.put("lon", order.getCoord().getLon());
        params.put("customer_id", order.getCustomer().getId().toString());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(warehouseUrl + WAREHOUSE_PATH, params, String.class);

//        restTemplate.postForObject(WAREHOUSE_URL, order, String.class);


//        StringBuilder stringBuilder = new StringBuilder("{");
//        stringBuilder.append("\"order_id\":\"").append(order.getId()).append("\",");
//        stringBuilder.append("\"item_id\":\"").append(order.getItem()).append("\",");
//        stringBuilder.append("\"lat\":\"").append(order.getCoord().getLat()).append("\",");
//        stringBuilder.append("\"lon\":\"").append(order.getCoord().getLon()).append("\",");
//        stringBuilder.append("\"customer_id\":\"").append(order.getCustomer().getId()).append("\"}");
//
//        try {
//            HttpURLConnection conn = (HttpURLConnection) new URL(warehouseUrl + WAREHOUSE_PATH).openConnection();
//            conn.setDoOutput(true);
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestMethod("POST");
//
//            try (OutputStream out = conn.getOutputStream()) {
//                out.write(stringBuilder.toString().getBytes());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return order;
    }

    @Override
    public NotificationMedium setPersonalPreferences(int customerId, NotificationMedium medium) {
        Optional<Customer> optCustomer = customerRepo.findById(customerId);

        if (!optCustomer.isPresent())
            throw new IllegalArgumentException("No Customer found with id " + customerId);

        Customer customer = optCustomer.get();
        customer.setMedium(medium);
        customerRepo.save(customer);

        return customer.getMedium();
    }

    @Override
    public int registerCustomer(String name, String firstName) {
        Customer customer = new Customer(name, firstName);
        customerRepo.save(customer);
        return customer.getId();
    }
}
