package fr.unice.polytech.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class Order {

    private @Getter @Setter String address;

    private @Getter @Setter String item;

    public Order(String address, String item) {
        this.address = address;
        this.item = item;
    }
}
