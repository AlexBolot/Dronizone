package fr.unice.polytech.codemara.warehouse.controller;

import fr.unice.polytech.codemara.warehouse.entities.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.repositories.OrderRepository;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/warehouse", produces = "application/json")
public class WarehouseController {

    private final Environment env;

    final
    OrderRepository orderRepository;

    public WarehouseController(Environment env, OrderRepository orderRepository) {
        this.env = env;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/orders")
    public String getAllOrders() {
        return orderRepository.findAll().toString();
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
            URL url = new URL(env.getProperty("DRONE_HOST"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            Map<String, String> parameters = new HashMap<>();
            parameters.put("orderid", "id");
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Message envoyé à un autre service <3";
    }

    /**Source
     * https://www.baeldung.com/java-http-request
     */
    static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }

}