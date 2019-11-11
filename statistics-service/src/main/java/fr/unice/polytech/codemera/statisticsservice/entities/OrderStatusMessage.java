package fr.unice.polytech.codemera.statisticsservice.entities;

public class OrderStatusMessage {

    private int order_id;
    private String status;

    @Override
    public String toString() {
        return "OrderStatusMessage{" +
                "order_id=" + order_id +
                ", status='" + status + '\'' +
                '}';
    }

    public OrderStatusMessage(int order_id, String status) {
        this.order_id = order_id;
        this.status = status;
    }

    public int getOrder_id() {
        return order_id;
    }

    public String getStatus() {
        return status;
    }
}
