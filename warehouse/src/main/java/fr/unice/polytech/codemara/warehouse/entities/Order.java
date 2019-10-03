package fr.unice.polytech.codemara.warehouse.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public @Data class Order {

    private String item;

    private long timestamp;

    private OrderStatus status;

}
