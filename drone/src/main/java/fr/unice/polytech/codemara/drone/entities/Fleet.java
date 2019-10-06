package fr.unice.polytech.codemara.drone.entities;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.Drone.*;
import static fr.unice.polytech.codemara.drone.entities.Drone.Status.ACTIVE;

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
        Status.find(status).ifPresent(droneStatus -> processForDrone(droneID, (drone -> drone.setStatus(droneStatus))));
    }

    public void updateData(long droneID, double batteryLevel, Whereabouts data) {
        processForDrone(droneID, drone -> {
            drone.setWhereabouts(data);
            drone.setBatteryLevel(batteryLevel);
        });
    }

    public String batteryPrettyDump() {
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        drones.forEach((id, drone) -> builder.append("\"").append(id).append("\": \"").append(drone.getBatteryLevel()).append("\","));

        // Remove last unnecessary ","
        return builder.toString().substring(0, builder.length() - 1) + "}";
    }

    private void processForDrone(long droneID, Consumer<Drone> consumer) {
        Drone drone = drones.get(droneID);
        consumer.accept(drone);
        drones.put(droneID, drone);
    }
}
