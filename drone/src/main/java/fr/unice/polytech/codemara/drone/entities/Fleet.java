package fr.unice.polytech.codemara.drone.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.Drone.*;
import static fr.unice.polytech.codemara.drone.entities.Drone.Status.ACTIVE;

@Getter
@Setter
public class Fleet {

    private Map<String, Drone> drones = new HashMap<>();

    public Fleet(Iterable<Drone> drones) {
        drones.forEach(drone -> this.drones.put(drone.getDroneID(), drone));
    }

    public List<Drone> getActiveFleet() {
        return drones.values().stream().filter(drone -> drone.is(ACTIVE)).collect(Collectors.toList());
    }

    public void changeStatus(String droneID, String status) {
        Status.find(status).ifPresent(droneStatus -> processForDrone(droneID, (drone -> drone.setStatus(droneStatus))));
    }

    public void updateData(String droneID, double batteryLevel, Whereabouts data) {
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

    private void processForDrone(String droneID, Consumer<Drone> consumer) {
        Drone drone = drones.get(droneID);
        consumer.accept(drone);
        drones.put(droneID, drone);
    }
}
