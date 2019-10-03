package fr.unice.polytech.codemara.drone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.Fleet;
import fr.unice.polytech.codemara.drone.entities.Whereabouts;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/drone", produces = "application/json")
public class DroneController {

    private static Fleet fleet = new Fleet();

    public DroneController() {
        List<Drone> drones = new ArrayList<>();
        for (int i = 0; i < 15; i++) drones.add(new Drone("aaa" + i, new Random().nextInt(100)));

        fleet = new Fleet(drones);
    }

    /**
     * Route dedicated to ask for the fleet's battery statuses
     *
     * @return JSON map containing the battery level of each drone of the fleet
     * <p>
     * # US-4 Elena can query [Mom document] to ask for the battery levels of the drone fleet
     */
    @RequestMapping(method = GET, path = "/fleet_battery_status")
    public String fleetBatteryStatus() {
        return fleet.batteryPrettyDump();
    }

    /**
     * Route dedicated to change a drone's status : active or aside from the fleet
     * # US-4 Elena can notify that a drone is set aside [Mom document]
     * # US-4 Elena can notify that sidelined drone is ready for service [Mom document]
     *
     * @param droneID Identifier of the Drone to remove from the fleet
     */
    @RequestMapping(method = PUT, path = "/set_drone_aside/{droneID}/{status}")
    public void changeDroneStatus(@PathVariable String droneID, @PathVariable String status) {
        fleet.changeStatus(droneID, status);
    }

    /**
     * Route dedicated for physical drones to update this service of their whereabouts
     * <p>
     * Expected JSON Format :
     *
     * <pre>
     *  {@code
     *  {
     *    "droneID": ...,
     *    "battery_level": ...,
     *    "whereabouts":
     *      {
     *        "latitude": ...,
     *        "longitude": ...,
     *        "altitude": ...,
     *        "distanceToTarget": ...
     *      }
     *  }
     *  }
     * </pre>
     * <p>
     * # US-3 The drones sends their positions, distance to target and battery levels regularly to the drone service ##
     * # US-3 When the distance goes below the 200m thresholds the drone service pings the order service ##
     *
     * @param json a JSON-parsed DroneData object
     */
    @RequestMapping(method = POST, path = "/update_battery_status", consumes = "application/json")
    public void updateBatteryStatus(@RequestBody String json) throws IOException {
        ObjectNode jsonNode = new ObjectMapper().readValue(json, ObjectNode.class);

        String droneId = jsonNode.get("droneID").asText();
        double batteryLevel = jsonNode.get("battery_level").asDouble();

        String whereaboutsJson = jsonNode.get("whereabouts").toString();

        Whereabouts whereabouts = new ObjectMapper().readValue(whereaboutsJson, Whereabouts.class);

        fleet.updateData(droneId, batteryLevel, whereabouts);

        if (whereabouts.getDistanceToTarget() < 200)
            System.out.println("Alert we are close to delivery zone, send notification");
    }
}
