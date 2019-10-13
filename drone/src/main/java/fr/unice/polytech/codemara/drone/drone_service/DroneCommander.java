package fr.unice.polytech.codemara.drone.drone_service;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Used to send commands to the drones
 */
public class DroneCommander {
    private final Environment env;
    private String externalDroneUrl;
    @Autowired
    DroneRepository droneRepository;
    private static final Logger logger = LoggerFactory.getLogger(DroneCommand.class);

    public DroneCommander(Environment env) {
        this.env= env;
    }

    public void broadcast(DroneCommand command) {
        for (Drone drone :
                this.getActiveDrones()) {
            this.sendCommand(command.copyWith(drone));
        }
    }

    /**
     * Send a command to a drone
     * @param command {@link DroneCommand}
     */
    public void sendCommand(DroneCommand command) {

        try {
            URL url = UriComponentsBuilder.fromUriString(env.getProperty("EXTERNAL_DRONE_HOST")+"/commands")
                    .build().toUri().toURL();
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response
                    = restTemplate.postForEntity(url.toString(),command, String.class);
            ObjectMapper mapper = new ObjectMapper();
            String body = response.getBody();
        } catch (MalformedURLException e) {
            logger.error(e.toString());
        }


    }


    private Iterable<Drone> getActiveDrones(){
        return droneRepository.getDronesByDroneStatus(DroneStatus.ACTIVE);
    }
}
