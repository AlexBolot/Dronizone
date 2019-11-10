package fr.unice.polytech.codemara.drone.entities;

import fr.unice.polytech.codemara.drone.entities.dto.DeliveryDTO;
import fr.unice.polytech.codemara.drone.entities.dto.DeliveryStatus;
import lombok.*;

import javax.persistence.*;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@With
@Embeddable
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private long orderId;

    private DeliveryStatus status;

    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude_pickup")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude_pickup"))
    })
    private Location deliveryLocation;

    private boolean notified = false;
    private boolean pickedUp = false;

    public Delivery(DeliveryDTO dto) {
        deliveryLocation = dto.getDeliveryLocation();
        orderId = dto.getOrderId();
        status = DeliveryStatus.valueOf(dto.getStatus());
    }

    public boolean mustNotify() {
        return !notified && pickedUp;
    }
}
