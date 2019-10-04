package fr.unice.polytech.entities;

import javax.persistence.*;

@Entity
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

    public Order() {}

    public Order(int id, Coord coord, Item item, Status status, Customer customer, String paymentInfo) {
        this.id = id;
        this.coord = coord;
        this.item = item;
        this.status = status;
        this.customer = customer;
        this.paymentInfo = paymentInfo;
    }

    public int getId() {
        return id;
    }

    public Coord getCoord() {
        return coord;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (getId() != order.getId()) return false;
        if (getCoord() != null ? !getCoord().equals(order.getCoord()) : order.getCoord() != null) return false;
        if (getItem() != null ? !getItem().equals(order.getItem()) : order.getItem() != null) return false;
        if (getStatus() != order.getStatus()) return false;
        if (getCustomer() != null ? !getCustomer().equals(order.getCustomer()) : order.getCustomer() != null)
            return false;
        return getPaymentInfo() != null ? getPaymentInfo().equals(order.getPaymentInfo()) : order.getPaymentInfo() == null;
    }

    @Override
    public int hashCode() {
        int result = getCoord() != null ? getCoord().hashCode() : 0;
        result = 31 * result + (getItem() != null ? getItem().hashCode() : 0);
        result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
        result = 31 * result + (getCustomer() != null ? getCustomer().hashCode() : 0);
        result = 31 * result + (getPaymentInfo() != null ? getPaymentInfo().hashCode() : 0);
        result = 31 * result + getId();
        return result;
    }

    @Override
    public String toString() {
        return "Order{" +
                "coord=" + coord +
                ", item=" + item +
                ", status=" + status +
                ", customer=" + customer +
                ", paymentInfo='" + paymentInfo + '\'' +
                ", id=" + id +
                '}';
    }
}
