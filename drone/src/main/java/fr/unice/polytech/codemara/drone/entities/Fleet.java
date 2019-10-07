package fr.unice.polytech.codemara.drone.entities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.ACTIVE;

/**
 * Represent a drone Fleet
 */
@Data
@Entity
@RequiredArgsConstructor
public class Fleet {

    @Id
    @GeneratedValue
    private long id;
    @OneToMany
    private Map<Long, Drone> drones = new HashMap<>();

    public Fleet(List<Drone> drones) {
        drones.forEach(drone -> this.drones.put(drone.getDroneID(), drone));
    }

    public List<Drone> getActiveFleet() {
        return drones.values().stream().filter(drone -> drone.is(ACTIVE)).collect(Collectors.toList());
    }

    public void changeStatus(long droneID, String status) {
        DroneStatus.find(status).ifPresent(droneStatus -> processForDrone(droneID, (drone -> drone.setDroneStatus(droneStatus))));
    }

    public void updateData(long droneID, double batteryLevel, Whereabouts data) {
        processForDrone(droneID, drone -> {
            drone.setWhereabouts(data);
            drone.setBatteryLevel(batteryLevel);
        });
    }



    private void processForDrone(long droneID, Consumer<Drone> consumer) {
        Drone drone = drones.get(droneID);
        consumer.accept(drone);
        drones.put(droneID, drone);
    }
}
