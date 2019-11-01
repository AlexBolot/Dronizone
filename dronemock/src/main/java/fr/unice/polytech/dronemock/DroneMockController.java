package fr.unice.polytech.dronemock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.dronemock.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static fr.unice.polytech.dronemock.models.DeliveryStatus.DELIVERING;
import static fr.unice.polytech.dronemock.models.DeliveryStatus.PICKING_UP;

@RestController
@RequestMapping(path = "/commands", produces = "application/json")
public class DroneMockController {

    private static final Logger logger = LoggerFactory.getLogger(DroneMockController.class);

    private static final String INIT_TYPE = "INITIALISATION";
    private static final String DELIVERY_TYPE = "DELIVERY";
    private static final String CALLBACK_TYPE = "CALLBACK";

    private Environment env;
    private KafkaTemplate kafkaTemplate;

    private final List<Drone> drones = new ArrayList<>();
    private List<JsonNode> commandHistory = new ArrayList<>();
    private Map<Drone, Location> bases = new HashMap<>();
    private Map<Drone, Delivery> deliveries = new HashMap<>();
    private Map<Drone, Boolean> pickups = new HashMap<>();
    private Map<Drone, Location> targets = new HashMap<>();

    public DroneMockController(Environment env, KafkaTemplate kafkaTemplate) {
        this.env = env;
        this.kafkaTemplate = kafkaTemplate;
        init();
    }

    private void init() {
        Drone drone = new Drone();
        drone.setBatteryLevel(10);
        drone.setDroneID(-10);
        drone.setWhereabouts(new Whereabouts(new Location(45, 7), 10, 0));
        drone.setDroneStatus(DroneStatus.ASIDE);
        this.drones.add(drone);
        drone = new Drone();
        drone.setBatteryLevel(10);
        drone.setDroneID(-11);
        drone.setWhereabouts(new Whereabouts(new Location(45, 7), 10, 0));
        drone.setDroneStatus(DroneStatus.ASIDE);
        this.drones.add(drone);
    }

    @GetMapping("/debug/{droneid}/finishPickup")
    public void finishPickup(@PathVariable(name = "droneid") long droneId) {
        Optional<Drone> resultDrone = getDroneById(droneId);
        Drone drone = resultDrone.orElseThrow(IllegalArgumentException::new);
        if (this.deliveries.containsKey(drone)) {
            drone.getWhereabouts().setLocation(this.deliveries.get(drone).getPickup_location());
        }
    }

    private Optional<Drone> getDroneById(long droneId) {
        return this.drones.stream().filter(d -> d.getDroneID() == droneId).findFirst();
    }

    @GetMapping("/debug/{droneid}/finishDelivery")
    public void finishDelivery(@PathVariable(name = "droneid") long droneId) {
        Optional<Drone> resultDrone = getDroneById(droneId);
        Drone drone = resultDrone.orElseThrow(IllegalArgumentException::new);
        if (this.deliveries.containsKey(drone)) {
            drone.getWhereabouts().setLocation(this.deliveries.get(drone).getTarget_location());
            deliveries.remove(drone);
        }
    }

    @GetMapping("/debug/drones")
    public List<Drone> getDrones() {
        return this.drones;
    }

    @GetMapping("/debug/{droneid}/delivery")
    public Delivery getDelivery(@PathVariable(name = "droneid") long droneId) {

        return this.deliveries.get(getDroneById(droneId).orElseThrow(IllegalAccessError::new));
    }

    @GetMapping("/debug/{droneid}/base")
    public Location getBase(@PathVariable(name = "droneid") long droneId) {
        // Because despite all pointing to the contrary our map does not find the drone in her keys, so doing the job of the drone map because i ain't got time to deal with this shit
        // FIXME: 25/10/2019 use the normal map mechanism
        return this.bases.entrySet().stream().filter(e -> e.getKey().getDroneID() == droneId).findFirst().orElseThrow(IllegalAccessError::new).getValue();

    }

    @GetMapping("/debug/commands")
    public List<JsonNode> getCommands() {
        return this.commandHistory;
    }

    @GetMapping("/debug/reset")
    public void reset() {
        this.drones.clear();
        this.deliveries.clear();
        this.init();
    }

    @Scheduled(fixedDelay = 1000)
    public void sendToDroneService() {

        drones.forEach(drone -> {
            try {
                update_drone_state(drone);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * Sends the update for a single drone
     *
     * @param drone
     * @throws JsonProcessingException
     */
    private void update_drone_state(Drone drone) throws JsonProcessingException {
        DroneState droneState = new DroneState(drone.getBatteryLevel(),
                drone.getWhereabouts(),
                drone.getDroneID(), drone.getDroneStatus(), System.currentTimeMillis());
        kafkaTemplate.send("drones", new ObjectMapper().writeValueAsString(droneState));
        System.out.println("publishing: " + new ObjectMapper().writeValueAsString(droneState));


        Location location = drone.getWhereabouts().getLocation();

        if (drone.getDroneStatus() == DroneStatus.ACTIVE) {

            if (this.targets.containsKey(drone)) {
                if (drone.getWhereabouts().getDistanceToTarget() < 100) {
                    Delivery delivery = deliveries.get(drone);
                    if (!this.pickups.get(drone)) {
                        this.pickups.put(drone, true);
                        this.targets.put(drone, delivery.getTarget_location());
                        // TODO send start delivery to Drone service
                        PickupState p = new PickupState(drone.getDroneID(), delivery.getOrderId(), delivery.getItemId(), PICKING_UP);
                        this.kafkaTemplate.send("drone-delivery-update", new ObjectMapper().writeValueAsString(p));
                    } else {
                        PickupState p = new PickupState(drone.getDroneID(), delivery.getOrderId(), delivery.getItemId(), DELIVERING);
                        drone.setDroneStatus(DroneStatus.ASIDE);
                        this.deliveries.remove(drone);
                        this.targets.remove(drone);
                        // TODO send delivery finish to Drone service
                        this.kafkaTemplate.send("drone-delivery-update", new ObjectMapper().writeValueAsString(p));
                    }
                } else {
                    moveDrone(location, this.targets.get(drone));
                }
            }
            setDistanceToTarget(drone, this.targets.get(drone));
        }

        if (drone.getDroneStatus() == DroneStatus.CALLED_HOME) {

            if (this.bases.containsKey(drone)) {
                Location base = this.bases.get(drone);
                double distanceToBase = distance(location.getLatitude(), base.getLatitude(),
                        location.getLongitude(), base.getLongitude(),
                        drone.getWhereabouts().getAltitude(),
                        drone.getWhereabouts().getAltitude());
                if (distanceToBase < 100) {
                    bases.remove(drone);
                } else {
                    moveDrone(location, base);
                }
            } else {
                if (drone.getBatteryLevel() < 100)
                    drone.setBatteryLevel(drone.getBatteryLevel() + 1);
            }
        }
    }

    @KafkaListener(topics = "drone-commands")
    public void receivedCommand(String message) {
        HashMap<String, Consumer<JsonNode>> commandTreatments = new HashMap<>();
        commandTreatments.put(INIT_TYPE, this::handleInitialisation);
        commandTreatments.put(DELIVERY_TYPE, this::handleDelivery);
        commandTreatments.put(CALLBACK_TYPE, this::handleCallback);
        try {
            JsonNode node = new ObjectMapper().readTree(message);
            String command = node.get("type").asText();
            this.commandHistory.add(node);
            commandTreatments.get(command).accept(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCallback(JsonNode node) {
        try {
            long id = extractCommandDroneid(node);
            Drone drone = this.getDroneById(id).orElseThrow(IllegalArgumentException::new);
            this.bases.put(drone, new ObjectMapper().readValue(node.get("baseLocation").toString(), Location.class));
            drone.setDroneStatus(DroneStatus.CALLED_HOME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelivery(JsonNode node) {
        try {
            long id = extractCommandDroneid(node);
            Delivery delivery = new ObjectMapper().readValue(node.get("delivery").toString(), Delivery.class);
            Drone drone = getDroneById(id).orElseThrow(IllegalAccessError::new);
            setDistanceToTarget(drone, delivery.getPickup_location());
            this.targets.put(drone, delivery.getPickup_location());
            drone.setDroneStatus(DroneStatus.ACTIVE);
            deliveries.put(drone, delivery);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Extract the drone id from a command
     *
     * @param node
     * @return
     */
    private long extractCommandDroneid(JsonNode node) {
        return node.get("target").get("droneID").asLong();
    }

    private void handleInitialisation(JsonNode node) {
        long id = extractCommandDroneid(node);
        Drone drone = getDroneById(id).orElseThrow(IllegalAccessError::new);
        drone.setDroneID(node.get("assignedId").asLong());
    }

    private void setDistanceToTarget(Drone drone, Location target) {
        Whereabouts whereabouts = drone.getWhereabouts();
        Location droneLocation = whereabouts.getLocation();

        whereabouts.setDistanceToTarget(distance(
                droneLocation.getLatitude(),
                target.getLatitude(),
                droneLocation.getLongitude(),
                target.getLongitude(),
                whereabouts.getAltitude(),
                whereabouts.getAltitude()));
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
