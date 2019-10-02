package fr.unice.polytech.codemera.mock_notification_service;

import fr.unice.polytech.codemera.mock_notification_service.notification.CustomerNotification;
import fr.unice.polytech.codemera.mock_notification_service.notification.OrderNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/notifications", produces = "application/json")
public class NotificationController {
    private final SimpMessageSendingOperations messagingTemplate;

    public NotificationController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/customer/{target_id}/order")

    public OrderNotification notify_customer_order(@PathVariable("target_id") long target_id, @RequestBody OrderNotification notification){
        System.out.println("customer_name = " + notification.customer_name);
        System.out.println("payload = " + notification.payload);
        System.out.println("order = " + notification.orderName);
        this.messagingTemplate.convertAndSend("/topic/notifications",notification);
        return notification;
    }
    @PostMapping("/customer/{target_id}")
    public CustomerNotification notify_customer(@PathVariable("target_id") long target_id, @RequestBody CustomerNotification notification){
        System.out.println("customer_name = " + notification.customer_name);
        System.out.println("payload = " + notification.payload);
        this.messagingTemplate.convertAndSend("/topic/notifications",notification);

        return notification;
    }
}
