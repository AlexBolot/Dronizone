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
    private int item_id;
    private String lat;
    private String lon;
    private int customer_id;

    public enum OrderStatus {
        PENDING,
        READY,
        TAKEN
    }

    private OrderStatus status;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int order_id;

    public void setStatus(OrderStatus orderStatus) {
        this.status = orderStatus;
    }

    public int getOrder_id() {
        return this.order_id;
    }
}
