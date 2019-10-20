package fr.unice.polytech.codemera.watchdog.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int order_id;
    private int customer_id;
    private OrderStatus status;
    private long timestamp;

    public enum OrderStatus {
        PENDING,
    }
}
