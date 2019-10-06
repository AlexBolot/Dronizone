package fr.unice.polytech.codemara.drone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.codemara.drone.drone_service.DroneCommander;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.entities.command.CommandType;
import fr.unice.polytech.codemara.drone.entities.command.DeliveryCommand;
import fr.unice.polytech.codemara.drone.entities.command.DroneCommand;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.security.krb5.internal.ktab.KeyTab;

import java.io.IOException;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/drone", produces = "application/json")
public class DroneController {

    private static Fleet fleet = new Fleet();
    final
    private DroneCommander droneCommander;
    private final DroneRepository droneRepository;
    private final DeliveryRepository deliveryRepository;

    public DroneController(DroneCommander droneCommander, DroneRepository droneRepository, DeliveryRepository deliveryRepository) {
        this.droneCommander = droneCommander;
        this.droneRepository = droneRepository;
        this.deliveryRepository = deliveryRepository;
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
    public void changeDroneStatus(@PathVariable long droneID, @PathVariable String status) {
        Optional<Drone> drone = droneRepository.findById(droneID);
        drone.ifPresent(d -> {
            d.setDroneStatus(DroneStatus.valueOf(status));
            droneRepository.save(d);
        } );
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

        long droneId = jsonNode.get("droneID").asLong(0);
        double batteryLevel = jsonNode.get("battery_level").asDouble();

        String whereaboutsJson = jsonNode.get("whereabouts").toString();

        Whereabouts whereabouts = new ObjectMapper().readValue(whereaboutsJson, Whereabouts.class);

        fleet.updateData(droneId, batteryLevel, whereabouts);

        if (whereabouts.getDistanceToTarget() < 200)
            System.out.println("Alert we are close to delivery zone, send notification");
    }
    @PutMapping(path = "/request_delivery")
    public void requireDelivery(@RequestBody Delivery delivery){
        deliveryRepository.save(delivery);
        Iterator<Drone> drones = droneRepository.getDroneByCurrentDelivery(null).iterator();
        Drone drone = null;
        if (drones.hasNext())
            drone = drones.next();
        DeliveryCommand deliveryCommand = new DeliveryCommand();
        deliveryCommand.setDelivery(delivery);
        deliveryCommand.setTarget(drone);
        droneCommander.sendCommand(deliveryCommand);
    }

    @PostMapping(path = "/fleet/command/callback")
    public void callbackFleet()
    {
        DroneCommand callbackCommand = new DroneCommand(CommandType.CALLBACK);
        droneCommander.broadcast(callbackCommand);

    }
}
