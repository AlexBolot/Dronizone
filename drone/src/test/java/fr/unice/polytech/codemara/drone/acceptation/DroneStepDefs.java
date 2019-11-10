package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.entities.dto.ShipmentDTO;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import fr.unice.polytech.codemara.drone.repositories.ShipmentRepository;
import fr.unice.polytech.codemara.drone.repositories.WhereaboutsRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.*;
import static fr.unice.polytech.codemara.drone.entities.dto.DeliveryStatus.DELIVERING;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Here are to be found the drone specific step defs, relating to data initilization, modification, and commands issuing
 * However drone state is handled in the {@link DroneStateStepDefs} class
 */
public class DroneStepDefs {

    @Autowired
    private Environment environment;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeliveryRepository deliveryRepository;

    private Drone activeDrone;

    @Autowired
    private WhereaboutsRepository whereaboutsRepository;
    private IntegrationContext context = IntegrationContext.getInstance();
    private BlockingQueue<ConsumerRecord<String, String>> shipmentRecords;


    @Given("An active Drone Fleet")
    public void anActiveDroneFleet() {
        this.anEmptyFleet();
        List<Drone> drones = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Drone drone = new Drone(new Random().nextInt(100));
            drone.setDroneStatus(ACTIVE);
            drones.add(drone);
        }

        droneRepository.saveAll(drones);
        this.context.currentDroneList = drones;
    }

    @And("a free drone")
    public void aFreeDrone() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone.setDroneStatus(DroneStatus.ACTIVE);
        drone.setCurrentDelivery(null);
        drone = droneRepository.save(drone);
        this.context.currentDrone = drone;
    }

    @And("A sidelined drone")
    public void aSidelinedDrone() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone.setDroneStatus(ASIDE);
        drone.setCurrentDelivery(null);
        drone = droneRepository.save(drone);
        this.context.currentDrone = drone;
    }

    @When("Elena callbacks the drones")
    public void elenaCallbacksTheDrones() throws Exception {
        mockMvc.perform(post("/drone/fleet/command/callback")).andExpect(status().isOk());
        Thread.sleep(10000);
    }

    @And("All drones states is Callback")
    public void allDronesStatesIsCallback() {
        droneRepository.findAll().forEach(drone -> assertEquals(CALLED_HOME, drone.getDroneStatus()));
    }

    @When("Klaus requires a delivery")
    public void klausRequiresADelivery() {
        try {
            Shipment shipment = new Shipment();
            Delivery delivery = new Delivery()
                    .withNotified(false)
                    .withPickedUp(false)
                    .withOrderId(1)
                    .withStatus(DELIVERING)
                    .withDeliveryLocation(new Location(10, 10));

            shipment.setDeliveries(Collections.singletonList(delivery));

            ShipmentDTO dto = new ShipmentDTO(shipment);

            String jsonDTO = new ObjectMapper().writeValueAsString(dto);
            this.context.currentShipment = shipment;
            this.context.currentDelivery = delivery;
            this.context.kafkaTemplate.send("shipment-packed", jsonDTO);
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @And("The sent delivery is registered")
    public void theSentDeliveryIsRegistered() {
        assertNotNull(shipmentRepository.findById(this.context.currentShipment.getId()));
        assertNotNull(deliveryRepository.findByOrderId(this.context.currentDelivery.getOrderId()));
    }

    @And("deliveries in completion")
    public void deliveriesInCompletion() {
        shipmentRepository.deleteAll();
        deliveryRepository.deleteAll();

        for (long i = 1; i < 16; i++) {
            Optional<Drone> result = this.droneRepository.findById(i);
            Drone drone = result.get();

            Shipment shipment = new Shipment();
            Delivery delivery = new Delivery()
                    .withNotified(false)
                    .withPickedUp(false)
                    .withOrderId(i)
                    .withStatus(DELIVERING)
                    .withDeliveryLocation(new Location(10, 10));

            delivery = deliveryRepository.save(delivery);
            shipment.setDeliveries(Collections.singletonList(delivery));

            shipment = shipmentRepository.save(shipment);
            drone.setCurrentShipment(shipment);
            drone.setCurrentDelivery(shipment.next());
            droneRepository.save(drone);
        }
    }

    @Given("An empty fleet")
    public void anEmptyFleet() {
        this.droneRepository.deleteAll(this.droneRepository.findAll());
    }

    @And("An empty DeliveryHistory")
    public void anEmptyDeliveryHistory() {
        this.shipmentRepository.deleteAll();
        this.deliveryRepository.deleteAll();
    }

    @Given("^A basic drone fleet$")
    public void aBasicDroneFleet() {
        this.anActiveDroneFleet();
    }

    @And("An active drone")
    public void anActiveDroneNamed() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone = droneRepository.save(drone);
        this.activeDrone = drone;
    }

    @When("Elena calls the active back to base")
    public void elenaCallsBackToBase() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + this.activeDrone.getDroneID() + "/" + DroneStatus.CALLED_HOME + "");
        mockMvc.perform(req).andExpect(status().isOk());
    }

    @Then("Drone active status is {string}")
    public void sStatusIs(String statusName) {
        DroneStatus status = DroneStatus.find(statusName).get();
        Iterable<Drone> drones = droneRepository.getDronesByDroneStatus(status);
        assertTrue(drones.iterator().hasNext());
        // assert drone has this status
    }

    @When("^Elena asks to set the drone aside$")
    public void elenaAsksToSetAside() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + this.context.currentDrone.getDroneID() + "/" + ASIDE + "");
        mockMvc.perform(req)
                .andExpect(status().isOk());
    }

    @Then("The drone's status is {string}")
    public void theDroneSStatusIs(String statusName) {
        this.context.currentDrone = droneRepository.findById(this.context.currentDrone.getDroneID()).get();
        DroneStatus status = DroneStatus.find(statusName).get();
        Assert.assertTrue(this.context.currentDrone.is(status));
    }

    @When("^Elena asks to set the drone ready for service$")
    public void elenaAsksToSetReadyForService() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + this.context.currentDrone.getDroneID() + "/" + ACTIVE + "");
        mockMvc.perform(req)
                .andExpect(status().isOk());
    }

    @Given("A delivering drone")
    public void aDeliveringDroneOfId() {
        this.context.currentDrone = new Drone();
        Delivery delivery = new Delivery();
        delivery.setDeliveryLocation(new Location(45, 7));
        delivery.setPickedUp(true); // Drone already picked up package
        deliveryRepository.save(delivery);
        this.context.currentDrone.setCurrentDelivery(delivery);
        Whereabouts whereabouts = new Whereabouts(0, new Location(45, 7), 100, 300);
        whereaboutsRepository.save(whereabouts);
        this.context.currentDrone.setWhereabouts(whereabouts);
        this.context.currentDrone.setBatteryLevel(100);
        this.context.currentDrone.setDroneStatus(ACTIVE);
        droneRepository.save(context.currentDrone);
    }

    @Given("A drone going to pickup")
    public void aDroneGoingToPickup() {
        this.context.currentDrone = new Drone();
        Delivery delivery = new Delivery();
        delivery.setDeliveryLocation(new Location(45, 7));
        delivery.setPickedUp(false); // Drone is going to pick up package
        deliveryRepository.save(delivery);
        this.context.currentDrone.setCurrentDelivery(delivery);
        Whereabouts whereabouts = new Whereabouts(0, new Location(45, 7), 100, 300);
        whereaboutsRepository.save(whereabouts);
        this.context.currentDrone.setWhereabouts(whereabouts);
        this.context.currentDrone.setBatteryLevel(100);
        this.context.currentDrone.setDroneStatus(ACTIVE);
        droneRepository.save(context.currentDrone);
    }

    @Then("The drone is added in the database")
    public void theDroneIsAddedInTheDatabase() {
        Iterable<Drone> all = droneRepository.findAll();
        int count = 0;
        for (Drone drone : all) count += 1;
        assertEquals(1, count);
    }

    @And("The drone is assigned a new id")
    public void theDroneIsAssignedANewId() {
        Drone drone = droneRepository.findAll().iterator().next();
        assertNotEquals(this.context.currentDrone.getDroneID(), drone.getDroneID());
    }

    @Given("A Mocked shipment-refused Listener")
    public void aMockedShipmentRefusedListener() {
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(System.getProperty("spring.kafka.bootstrap-servers"),
                        "test", "false");
        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerProperties);
        // set the topic that needs to be consumed
        ContainerProperties containerProperties = new ContainerProperties("shipment-refused");
        if (this.context.shipmentContainer != null) {
            this.context.shipmentContainer.stop();
        }
        // create a Kafka MessageListenerContainer
        this.context.shipmentContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        // create a thread safe queue to store the received message
        shipmentRecords = new LinkedBlockingQueue<>();
        // setup a Kafka message listener
        this.context.shipmentContainer
                .setupMessageListener((MessageListener<String, String>) record -> shipmentRecords.add(record));
        // start the container and underlying message listener
        this.context.shipmentContainer.start();
        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(this.context.shipmentContainer, 1);
    }

    @Then("The delivery of the shipment is refused")
    public void theDeliveryOfTheShipmentIsRefused() throws InterruptedException {

        Thread.sleep(1000);
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        shipmentRecords.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        assertEquals(1, actual.size());
        this.context.shipmentContainer.stop();
        shipmentRecords.clear();
    }
}
