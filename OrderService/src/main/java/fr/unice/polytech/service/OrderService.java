package fr.unice.polytech.service;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import fr.unice.polytech.entities.Order;

@JsonRpcService("/order")
public interface OrderService {
    Order orderItem(@JsonRpcParam(value = "item") int itemId,
                    @JsonRpcParam(value = "customer") int customerId,
                    @JsonRpcParam(value = "lon") String lon,
                    @JsonRpcParam(value = "lat") String lat,
                    @JsonRpcParam(value = "payment_info") String paymentInfo);
}
