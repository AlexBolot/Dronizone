package fr.unice.polytech.dronemock;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.dronemock.models.DroneState;
import fr.unice.polytech.dronemock.models.Location;
import fr.unice.polytech.dronemock.models.Whereabouts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/commands", produces = "application/json")
public class DroneMockController {

    private final Environment env;
    private final KafkaTemplate kafkaTemplate;


    private static final Logger logger = LoggerFactory.getLogger(DroneMockController.class);

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
    }

    @PostMapping()
    public String received(@RequestBody String body) {
        return "OK";
    }

    @Scheduled(fixedDelay = 500)
    public void sendToDroneService() throws IOException {
        DroneState droneState = new DroneState(100,new Whereabouts(new Location(43.6, 7.1),100,100),1);
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
