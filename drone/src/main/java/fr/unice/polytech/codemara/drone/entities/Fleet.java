package fr.unice.polytech.codemara.drone.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.ACTIVE;

@Getter
@Setter
public class Fleet {

    private Map<String, Drone> drones = new HashMap<>();

    public Fleet() {
    }

    public Fleet(List<Drone> drones) {
        drones.forEach(drone -> this.drones.put(drone.getDroneID(), drone));
    }

    public List<Drone> getActiveFleet() {
        return drones.values().stream().filter(drone -> drone.is(ACTIVE)).collect(Collectors.toList());
    }

    public void changeStatus(String droneID, String status) {
        DroneStatus.find(status).ifPresent(droneStatus -> processForDrone(droneID, (drone -> drone.setDroneStatus(droneStatus))));
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

        String dump = builder.toString();

        // Remove last unnecessary ","
        dump = dump.substring(0, dump.length() - 1) + "}";

        return dump;
    }

    private void processForDrone(String droneID, Consumer<Drone> consumer) {
        Drone drone = drones.get(droneID);
        consumer.accept(drone);
        drones.put(droneID, drone);
    }
}
