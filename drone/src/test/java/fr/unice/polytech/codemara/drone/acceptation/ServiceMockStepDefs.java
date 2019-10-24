package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.entities.command.CommandType;
import fr.unice.polytech.codemara.drone.entities.command.DeliveryCommand;
import fr.unice.polytech.codemara.drone.entities.command.DroneCommand;
import fr.unice.polytech.codemara.drone.entities.command.InitCommand;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mockserver.client.MockServerClient;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.ACTIVE;
import static fr.unice.polytech.codemara.drone.entities.DroneStatus.CALLED_HOME;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Here a regrouped all step defs relating to service mock, from initialization, to stubing, verification and tear down
 */
public class ServiceMockStepDefs {

    private IntegrationContext context = IntegrationContext.getInstance();
    private LinkedBlockingQueue<ConsumerRecord<String, String>> records;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DroneRepository droneRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;


    @And("A mocked Order Service")
    public void aMockedOrderService() {
        int serverPort = 20000;
        System.setProperty("ORDER_SERVICE_HOST", "http://localhost:" + serverPort + "/");
        if (this.context.clientServer == null) {
            this.context.clientServer = startClientAndServer(serverPort);
            this.context.mockServer = new MockServerClient("localhost", serverPort);
        }
        this.context.mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/order/notify/cancel/.*")
                ).respond(response(""));
        this.context.mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/order/notify/delivery/.*")
                ).respond(response(""));
        ;
    }

    @Then("^The OrderService receives (\\d+) delivery notification$")
    public void theOrderServiceReceivesANotification(int notificationCount) throws InterruptedException {
        Thread.sleep(1000);
        this.context.mockServer.verify(
                request().withPath("/order/notify/delivery/" + this.context.currentDrone.getCurrentDelivery().getOrderId()).withMethod("GET"),
                VerificationTimes.exactly(notificationCount)
        );
    }


    @And("The mock server is teared down")
    public void theMockServerIsTearedDown() {
        System.out.println("teardown");
        if (this.context.clientServer != null) {
            this.context.clientServer.stop();
            this.context.clientServer = null;
        }
        this.context.mockServer = null;
    }

    @And("A Mocked External Drone Commander")
    public void aMockedExternalDroneCommander() {
        int serverPort = 20000;
        System.setProperty("EXTERNAL_DRONE_HOST", "http://localhost:" + serverPort + "/");
        if (this.context.clientServer == null) {
            serverPort = 20000;
            this.context.clientServer = startClientAndServer(serverPort);
            context.mockServer = new MockServerClient("localhost", serverPort);
        }
        context.mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/commands")
                )
                .respond(
                        httpRequest -> {
                            try {
                                JsonNode command = new ObjectMapper().readTree(httpRequest.getBodyAsString());
                                if (command.path("type").textValue().equals("CALLBACK")) {
                                    if (context.kafkaTemplate != null) {
                                        DroneState droneState = new DroneState(100, new Whereabouts(0, new Location(45, 7), 100, 200), command.path("target").path("droneID").asLong(), CALLED_HOME, System.currentTimeMillis());
                                        context.kafkaTemplate.send("drones", new ObjectMapper().writeValueAsString(droneState));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return response("");
                        }
                );
    }

    @Then("A delivery canceled notification is sent to the order service for each delivery")
    public void aDeliveryCanceledNotificationIsSentToTheOrderService() {

        for (Delivery delivery : deliveryRepository.findAll()) {
            this.context.mockServer.verify(
                    request().withPath("/order/notify/cancel/" + delivery.getOrderId()).withMethod("GET")
            );
        }
    }


    @Then("A Callback Command is Issued for all drones")
    public void aCallbackCommandIsIssuedForAllDrones() throws JsonProcessingException {
        List<String> expected = new ArrayList<>();
        for (Drone drone :
                droneRepository.findAll()) {
            ObjectMapper mapper = new ObjectMapper();
            drone.setDroneStatus(ACTIVE);
            String droneCallbackJson = mapper.writeValueAsString(new DroneCommand(CommandType.CALLBACK).copyWith(drone));
            // check that the message was received
            expected.add(droneCallbackJson);
        }
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        records.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        for (String droneCallbackJson :
                expected) {
            assertTrue(actual.contains(droneCallbackJson));
        }

    }

    @And("mocked drone listener")
    public void mockedDroneListener() {
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(System.getProperty("spring.kafka.bootstrap-servers"),
                        "mocked-consumer", "false");
        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<String, String>(
                        consumerProperties);

        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties("drones-commands", "drones");
        if (this.context.container != null) {
            this.context.container.stop();
        }
        // create a Kafka MessageListenerContainer
        this.context.container = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>();

        // setup a Kafka message listener
        this.context.container
                .setupMessageListener(new MessageListener<String, String>() {
                    @Override
                    public void onMessage(ConsumerRecord<String, String> record) {

                        records.add(record);
                    }
                });

        // start the container and underlying message listener
        this.context.container.start();

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(this.context.container,
                Integer.parseInt(System.getProperty("spring.kafka.partitions-per-topics")) * 2);
    }
    @Then("A delivery command is sent to an available drone")
    public void aDeliveryCommandIsSentToAnAvailableDrone() throws JsonProcessingException {
        DeliveryCommand deliveryCommand = new DeliveryCommand(this.context.currentDrone,
                deliveryRepository.findByOrderIdAndItemId(this.context.currentDelivery.getOrderId(), this.context.currentDelivery.getItemId()));

        String expected = new ObjectMapper().writeValueAsString(deliveryCommand);
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        records.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        assertTrue(actual.contains(expected));
    }

    @And("A Drone initialization command is sent to the drone")
    public void aDroneInitializationCommandIsSentToTheDrone() throws JsonProcessingException {
        Drone drone = droneRepository.findAll().iterator().next();
        long newId = drone.getDroneID();
        drone.setDroneID(this.context.currentDrone.getDroneID());
        InitCommand initializationCommand = new InitCommand(drone,newId);
        String expected = new ObjectMapper().writeValueAsString(initializationCommand);
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        records.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        assertTrue(actual.contains(expected));
    }

    @And("A pause of {int} seconds")
    public void aPauseOfSeconds(int arg0) throws InterruptedException {
        Thread.sleep(arg0*1000);
    }
}
