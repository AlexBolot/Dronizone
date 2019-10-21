package fr.unice.polytech.dronemock;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.dronemock.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Random;

@RestController
@RequestMapping(path = "/commands", produces = "application/json")
public class DroneMockController {

    private final Environment env;
    private final KafkaTemplate kafkaTemplate;


    private static final Logger logger = LoggerFactory.getLogger(DroneMockController.class);
    private final Drone currentDrone;

    private int batteryLevel;
    private int droneId;
    private double lon;
    private double lat;
    private int alt;
    private int distanceToTarget;

    private final static String DRONE_PATH = "/drone/update_battery_status";

    //    private final Arraylist<> commandHistory;
    public DroneMockController(Environment env, KafkaTemplate kafkaTemplate) {
        batteryLevel = 80;
        droneId = 2;
        lon = 10.;
        lat = 10.;
        alt = 12;
        distanceToTarget = 100;
        this.env = env;
        this.kafkaTemplate = kafkaTemplate;
        this.currentDrone = new Drone();
        currentDrone.setBatteryLevel(10);
        currentDrone.setDroneID(-10);
        currentDrone.setWhereabouts(new Whereabouts(new Location(45, 7), 10, 200));
        currentDrone.setDroneStatus(DroneStatus.ACTIVE);
    }

    @PostMapping()
    public String received(@RequestBody String body) {
        return "OK";
    }

    @Scheduled(fixedDelay = 500)
    public void sendToDroneService() throws IOException {
        DroneState droneState = new DroneState(this.currentDrone.getBatteryLevel(),
                this.currentDrone.getWhereabouts(),
                this.currentDrone.getDroneID(), this.currentDrone.getDroneStatus(), System.currentTimeMillis());
        kafkaTemplate.send("drones",new ObjectMapper().writeValueAsString(droneState));
        System.out.println("publishing: " + new ObjectMapper().writeValueAsString(droneState));
        Location location = this.currentDrone.getWhereabouts().getLocation();
        this.currentDrone.getWhereabouts().getLocation().setLatitude(location.getLatitude() - 0.001 * new Random().nextInt(9));
        this.currentDrone.getWhereabouts().getLocation().setLongitude(location.getLongitude() - 0.001 * new Random().nextInt(9));
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
