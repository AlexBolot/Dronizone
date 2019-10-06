package fr.unice.polytech.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import fr.unice.polytech.entities.*;
import fr.unice.polytech.repo.CoordRepo;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import fr.unice.polytech.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@AutoJsonRpcServiceImpl
public class OrderServiceImpl implements OrderService {

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
        customerRepo.save(order.getCustomer());
        coordRepo.save(order.getCoord());
        orderRepo.save(order);

        String warehouseUrl = env.getProperty("WAREHOUSE_HOST");

//        URL url = null;
//        try {
//            url = UriComponentsBuilder.fromUriString(warehouseUrl + WAREHOUSE_PATH)
//                    .queryParam("order_id",order.getId())
//                    .queryParam("item_id",order.getItem().getId())
//                    .queryParam("lat",order.getCoord().getLat())
//                    .queryParam("lon",order.getCoord().getLon())
//                    .queryParam("customer_id",order.getCustomer().getId())
//                    .build().toUri().toURL();
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response
//                = restTemplate.postForEntity(url.toString(), String.class);
//        ObjectMapper mapper = new ObjectMapper();
//        String body = response.getBody();
//        JsonNode root = mapper.readTree(body);
//        JsonNode lyricsJson = root.path("message").path("body").path("lyrics").path("lyrics_body");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        restTemplate.postForObject(WAREHOUSE_URL, order, String.class);

        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"order_id\":\"").append(order.getId()).append("\",");
        stringBuilder.append("\"item_id\":\"").append(order.getItem()).append("\",");
        stringBuilder.append("\"lat\":\"").append(order.getCoord().getLat()).append("\",");
        stringBuilder.append("\"lon\":\"").append(order.getCoord().getLon()).append("\",");
        stringBuilder.append("\"customer_id\":\"").append(order.getCustomer().getId()).append("\"}");

        if (warehouseUrl == null) warehouseUrl = WAREHOUSE_URL;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(warehouseUrl + WAREHOUSE_PATH).openConnection();
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
