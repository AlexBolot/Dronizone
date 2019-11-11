package fr.unice.polytech.codemera.mock_notification_service;

import fr.unice.polytech.codemera.mock_notification_service.notification.CustomerNotification;
import fr.unice.polytech.codemera.mock_notification_service.notification.Notification;
import fr.unice.polytech.codemera.mock_notification_service.notification.OrderNotification;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/notifications", produces = "application/json")
public class NotificationController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final List<Notification> notificationHistory = new ArrayList<>();
    public NotificationController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/customer/{target_id}/order")
    public OrderNotification notify_customer_order(@PathVariable("target_id") long target_id, @RequestBody OrderNotification notification){
        System.out.println("customer_name = " + notification.customer_name);
        System.out.println("payload = " + notification.payload);
        System.out.println("item_name = " + notification.item_name);
        System.out.println("medium = " + notification.medium);
        this.messagingTemplate.convertAndSend("/topic/notifications", notification);
        this.notificationHistory.add(notification);
        return notification;
    }

    @PostMapping("/customer/{target_id}")
    public CustomerNotification notify_customer(@PathVariable("target_id") long target_id, @RequestBody CustomerNotification notification){
        System.out.println("customer_name = " + notification.customer_name);
        System.out.println("payload = " + notification.payload);
        System.out.println("medium = " + notification.medium);
        this.messagingTemplate.convertAndSend("/topic/notifications", notification);
        this.notificationHistory.add(notification);
        return notification;
    }

    @GetMapping("/mock/notification_history")
    public List<Notification> getNotificationHistory() {
        return this.notificationHistory;
    }
}
