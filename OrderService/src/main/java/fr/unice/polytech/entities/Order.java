package fr.unice.polytech.entities;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@Table(name = "order_item")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @OneToOne
    private Coord coord;
    @OneToOne
    private Item item;
    private Status status;
    @OneToOne
    private Customer customer;

    private String paymentInfo;

    public Order(Coord coord, Item item, Status status, Customer customer, String paymentInfo) {
        this.coord = coord;
        this.item = item;
        this.status = status;
        this.customer = customer;
        this.paymentInfo = paymentInfo;
    }
}
