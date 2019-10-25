package fr.unice.polytech.codemera.watchdog.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_item")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int orderId;
    private int customerId;
    private OrderStatus status;
    private long timestamp;

    public enum OrderStatus {
        PENDING,
    }
}
