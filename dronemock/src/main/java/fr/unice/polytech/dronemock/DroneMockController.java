package fr.unice.polytech.dronemock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.dronemock.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/commands", produces = "application/json")
public class DroneMockController {

    private static final Logger logger = LoggerFactory.getLogger(DroneMockController.class);

    @Autowired
    private Environment env;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    private final Drone currentDrone;

    private boolean deliveryPickedUp = false;

    private Delivery currentDelivery;

    private Location target;

    private Location base;
    private List<JsonNode> commandHistory = new ArrayList<>();

    public DroneMockController() {
        currentDrone = new Drone();
        currentDrone.setBatteryLevel(10);
        currentDrone.setDroneID(-10);
        currentDrone.setWhereabouts(new Whereabouts(new Location(45, 7), 10, 0));
        currentDrone.setDroneStatus(DroneStatus.ASIDE);
    }

    @GetMapping("/debug/finishPickup")
    public void finishPickup() {
        if (this.currentDelivery != null && !this.deliveryPickedUp) {
            this.currentDrone.getWhereabouts().setLocation(this.currentDelivery.getPickup_location());
        }
    }

    @GetMapping("/debug/finishDelivery")
    public void finishDelivery() {
        if (this.currentDelivery != null && this.deliveryPickedUp) {
            this.currentDrone.getWhereabouts().setLocation(this.currentDelivery.getTarget_location());
        }
    }

    @GetMapping("/debug/id")
    public long getId() {
        return this.currentDrone.getDroneID();
    }

    @GetMapping("/debug/delivery")
    public Delivery getDelivery() {
        return this.currentDelivery;
    }

    @GetMapping("/debug/base")
    public Location getBase() {
        return this.base;
    }

    @GetMapping("/debug/commands")
    public List<JsonNode> getCommands() {
        return this.commandHistory;
    }

    @GetMapping("/debug/reset")
    public void reset() {
        this.currentDrone.setBatteryLevel(10);
        this.currentDrone.setDroneID(-10);
        this.currentDrone.setWhereabouts(new Whereabouts(new Location(45, 7), 10, 0));
        this.currentDrone.setDroneStatus(DroneStatus.ASIDE);
        this.currentDelivery = null;
        this.base = null;
    }

    @Scheduled(fixedDelay = 1000)
    public void sendToDroneService() throws IOException {
        DroneState droneState = new DroneState(this.currentDrone.getBatteryLevel(),
                this.currentDrone.getWhereabouts(),
                this.currentDrone.getDroneID(), this.currentDrone.getDroneStatus(), System.currentTimeMillis());
        kafkaTemplate.send("drones", new ObjectMapper().writeValueAsString(droneState));
        System.out.println("publishing: " + new ObjectMapper().writeValueAsString(droneState));


        Location location = this.currentDrone.getWhereabouts().getLocation();

        if (this.currentDrone.getDroneStatus() == DroneStatus.ACTIVE) {
            if (this.target != null) {
                if (this.currentDrone.getWhereabouts().getDistanceToTarget() < 100) {
                    if (!this.deliveryPickedUp) {
                        this.deliveryPickedUp = true;
                        this.target = this.currentDelivery.getTarget_location();
                        // TODO send start delivery to Drone service
                        PickupState p = new PickupState(this.currentDrone.getDroneID(), this.currentDelivery.getOrderId(), this.currentDelivery.getItemId());
                        this.kafkaTemplate.send("drones-pickups", new ObjectMapper().writeValueAsString(p));
                    } else {
                        PickupState p = new PickupState(this.currentDrone.getDroneID(), this.currentDelivery.getOrderId(), this.currentDelivery.getItemId());
                        this.deliveryPickedUp = false;
                        this.currentDrone.setDroneStatus(DroneStatus.ASIDE);
                        this.currentDelivery = null;
                        this.target = null;
                        // TODO send delivery finish to Drone service
                        this.kafkaTemplate.send("drones-deliveries", new ObjectMapper().writeValueAsString(p));
                    }
                } else {
                    moveDrone(location, target);
                }
            }
            setDistanceToTarget(this.target);
        }

        if (this.currentDrone.getDroneStatus() == DroneStatus.CALLED_HOME) {
            if (base != null) {
                double distanceToBase = distance(location.getLatitude(), this.base.getLatitude(),
                        location.getLongitude(), this.base.getLongitude(),
                        this.currentDrone.getWhereabouts().getAltitude(),
                        this.currentDrone.getWhereabouts().getAltitude());
                if (distanceToBase < 100) {
                    this.base = null;
                } else {
                    moveDrone(location, this.base);
                }
            } else {
                if (this.currentDrone.getBatteryLevel() < 100)
                    this.currentDrone.setBatteryLevel(this.currentDrone.getBatteryLevel() + 1);
            }
        }
    }

    @KafkaListener(topics = "drones-commands")
    public void receivedCommand(String message) {
        System.out.println(message);
        try {
            JsonNode node = new ObjectMapper().readTree(message);
            String command = node.get("type").asText();

            this.commandHistory.add(node);
            switch (command) {
                case "INITIALISATION":
                    this.currentDrone.setDroneID(node.get("assignedId").asLong());
                    break;
                case "DELIVERY":
                    this.currentDelivery = new ObjectMapper().readValue(node.get("delivery").toString(), Delivery.class);
                    setDistanceToTarget(this.currentDelivery.getPickup_location());
                    this.target = this.currentDelivery.getPickup_location();
                    this.currentDrone.setDroneStatus(DroneStatus.ACTIVE);
                    break;
                case "CALLBACK":
                    this.base = new ObjectMapper().readValue(node.get("baseLocation").toString(), Location.class);
                    this.currentDrone.setDroneStatus(DroneStatus.CALLED_HOME);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDistanceToTarget(Location target) {
        this.currentDrone.getWhereabouts().setDistanceToTarget(distance(
                this.currentDrone.getWhereabouts().getLocation().getLatitude(),
                target.getLatitude(),
                this.currentDrone.getWhereabouts().getLocation().getLongitude(),
                target.getLongitude(),
                this.currentDrone.getWhereabouts().getAltitude(),
                this.currentDrone.getWhereabouts().getAltitude()));
    }

    /*
     * returns random integer between minimum and maximum range
     */
    private int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    private void moveDrone(Location location, Location target) {
        if (location.getLatitude() >= target.getLatitude()) {
            if (location.getLongitude() >= target.getLongitude()) {
                location.setLatitude(location.getLatitude() - 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() - 0.001 * getRandomInteger(9, 1));
            } else {
                location.setLatitude(location.getLatitude() - 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() + 0.001 * getRandomInteger(9, 1));
            }
        } else {
            if (location.getLongitude() >= target.getLongitude()) {
                location.setLatitude(location.getLatitude() + 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() - 0.001 * getRandomInteger(9, 1));
            } else {
                location.setLatitude(location.getLatitude() + 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() + 0.001 * getRandomInteger(9, 1));
            }
        }
    }

    private double distance(double lat1, double lat2, double lon1,
                            double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}

//{
//    "droneID":"aaa2",
//    "battery_level":80,
//    "whereabouts":{
//        "location":{
//            "latitude":10.0,
//            "longitude":10.0
//        },
//        "altitude":12,
//        "distanceToTarget":250
//    }
//}
