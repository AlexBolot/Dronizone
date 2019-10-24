package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

/**
 * All Drone state updater stepdefs should be here
 */
public class DroneStateStepDefs {


    private IntegrationContext context = IntegrationContext.getInstance();

    @And("^The drone has distance to target of (\\d+)m$")
    public void theDroneHasDistanceToTargetOfM(int distance) throws JsonProcessingException {
        DroneState state = new DroneState(100, new Whereabouts(10, new Location(45, 7), 100, distance), this.context.currentDrone.getDroneID(), DroneStatus.ACTIVE, System.currentTimeMillis());
        context.kafkaTemplate.send("drones", new ObjectMapper().writeValueAsString(state));
    }

    @When("^The distance goes under (\\d+)m$")
    public void theDistanceGoesUnderM(int distance) throws JsonProcessingException {
        DroneState data = new DroneState(90, new Whereabouts(10, new Location(45, 7), 100, distance - 1), this.context.currentDrone.getDroneID(), DroneStatus.ACTIVE, System.currentTimeMillis());
        context.kafkaTemplate.send("drones", new ObjectMapper().writeValueAsString(data));
    }

    @And("mocked drone publishers")
    public void aDroneKafkaTopic() {
        // set up the Kafka producer properties
        Map<String, Object> senderProperties =
                KafkaTestUtils.senderProps(
                        System.getProperty("spring.kafka.bootstrap-servers"));

        // create a Kafka producer factory
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        senderProperties);

        // create a Kafka template
        context.kafkaTemplate = new KafkaTemplate<>(producerFactory);

    }

    @When("A new drone sends a state update")
    public void aNewDroneSendsAStateUpdate() throws JsonProcessingException {
        Whereabouts whereabouts = new Whereabouts();
        whereabouts.setDistanceToTarget(300);
        whereabouts.setLocation(new Location(45,7));
        whereabouts.setAltitude(100);
        DroneState data = new DroneState(90,
                whereabouts,
                -10
                , DroneStatus.ACTIVE, System.currentTimeMillis());
        context.kafkaTemplate.send("drones", new ObjectMapper().writeValueAsString(data));
        this.context.currentDrone = new Drone();
        this.context.currentDrone.setDroneID(-10);
    }
}
