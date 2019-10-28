package fr.unice.polytech.codemara.drone.drone_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.DroneStatus;
import fr.unice.polytech.codemara.drone.entities.command.DroneCommand;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to send commands to the drones
 */
public class DroneCommander {
    private final Environment env;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DroneCommand.class);

    public DroneCommander(Environment env) {
        this.env = env;
    }

    public void broadcast(DroneCommand command) {
        for (Drone drone : this.getActiveDrones()) {
            this.sendCommand(command.copyWith(drone));
        }
    }

    /**
     * Send a command to a drone
     *
     * @param command {@link DroneCommand}
     */
    public void sendCommand(DroneCommand command) {

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("command", command.getType().toString());
            params.put("droneId", command.getTarget().getDroneID());
            params.put("payload", command.getPayload());
            kafkaTemplate.send("drone-commands", new ObjectMapper().writeValueAsString(params));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    private Iterable<Drone> getActiveDrones() {
        return droneRepository.getDronesByDroneStatus(DroneStatus.ACTIVE);
    }
}
