package fr.unice.polytech.service;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import fr.unice.polytech.entities.Order;

@JsonRpcService("/order")
public interface OrderService {
    Order orderItem(@JsonRpcParam(value = "order") Order order);
}
