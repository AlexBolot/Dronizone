package fr.unice.polytech.codemara.warehouse.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Data
@Setter
@Getter
public class CustomerOrder {
    private int itemId;
    private int customerId;
    @Embedded
    private Coord deliveryLocation;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int orderId;

    public enum OrderStatus {
        PENDING,
        READY,
        TAKEN
    }

    private OrderStatus status;

    public void setStatus(OrderStatus orderStatus) {
        this.status = orderStatus;
    }

    public int getOrderId() {
        return this.orderId;
    }
}
