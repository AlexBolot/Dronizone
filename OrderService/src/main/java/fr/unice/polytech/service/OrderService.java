package fr.unice.polytech.service;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import fr.unice.polytech.entities.NotificationMedium;
import fr.unice.polytech.entities.Order;

@JsonRpcService("/order")
public interface OrderService {
    Order orderItem(@JsonRpcParam(value = "order") Order order);

    NotificationMedium setPersonalPreferences(@JsonRpcParam(value = "customerId") int customerId, @JsonRpcParam(value = "notificationPreference") NotificationMedium notificationMedium);

    int registerCustomer(@JsonRpcParam(value = "name") String name, @JsonRpcParam(value = "firstName") String firstName);
}