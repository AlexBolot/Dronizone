package fr.unice.polytech.dronemock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/commands", produces = "application/json")
public class DroneMockController {

    @Autowired
    private Environment env;


    private static final Logger logger = LoggerFactory.getLogger(DroneMockController.class);

    private int batteryLevel;
    private int droneId;
    private double lon;
    private double lat;
    private int alt;
    private int distanceToTarget;

    private final static String DRONE_PATH = "/drone/update_battery_status";

    public DroneMockController() {
        batteryLevel = 80;
        droneId = 2;
        lon = 10.;
        lat = 10.;
        alt = 12;
        distanceToTarget = 100;
    }

    @PostMapping()
    public String received(@RequestBody String body) {
        logger.info(body);
        return "OK";
    }

    @Scheduled(fixedDelay = 500)
    public void sendToDroneService() throws IOException {
        if (distanceToTarget > 1) distanceToTarget--;

        String droneServiceUrl = env.getProperty("DRONE_SERVICE");

        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"droneID\":\"").append(droneId).append("\",");
        stringBuilder.append("\"battery_level\":\"").append(batteryLevel).append("\",");
        stringBuilder.append("\"whereabouts\":{\"location\":{\"latitude\":").append(lat).append(",");
        stringBuilder.append("\"longitude\":\"").append(lon).append("\"},");
        stringBuilder.append("\"altitude\":\"").append(alt).append("\",");
        stringBuilder.append("\"distanceToTarget\":\"").append(distanceToTarget).append("\"}}");
        ObjectMapper mapper = new ObjectMapper();

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(droneServiceUrl + DRONE_PATH, mapper.readTree(stringBuilder.toString()), String.class);
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
