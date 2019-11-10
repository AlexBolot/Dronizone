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
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to send commands to the drones
 */
public class DroneCommander {


    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private KafkaTemplate kafkaTemplate;
    private Logger logger = LoggerFactory.getLogger(DroneCommander.class);


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
            params.put("command", command.getCommand().toString());
            params.put("droneId", command.getTarget().getDroneID());
            params.put("payload", command.getPayload());
            String kafkaCommand = new ObjectMapper().writeValueAsString(command);
            kafkaTemplate.send("drone-commands", kafkaCommand);
            logger.info("sending command : {}", kafkaCommand);
        } catch (JsonProcessingException e) {
            logger.error("DroneCommander.sendcommand", e);

        }
    }


    private Iterable<Drone> getActiveDrones() {
        return droneRepository.getDronesByDroneStatus(DroneStatus.ACTIVE);
    }
}
