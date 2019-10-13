package fr.unice.polytech.dronemock;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.dronemock.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
        currentDrone.setBatteryLevel(100);
        currentDrone.setDroneID(-10);
        currentDrone.setWhereabouts(new Whereabouts(new Location(45,7),100,300));
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
                this.currentDrone.getDroneID(), this.currentDrone.getDroneStatus());
        kafkaTemplate.send("drones",new ObjectMapper().writeValueAsString(droneState));
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
