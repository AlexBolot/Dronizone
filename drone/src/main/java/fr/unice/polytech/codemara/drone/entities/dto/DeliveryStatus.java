package fr.unice.polytech.codemara.drone.entities.dto;

public enum DeliveryStatus {
    DELIVERING, DELIVERED, PICKING_UP;

    public static boolean is(String statusName, DeliveryStatus status){
        return valueOf(statusName) == status;
    }
}
