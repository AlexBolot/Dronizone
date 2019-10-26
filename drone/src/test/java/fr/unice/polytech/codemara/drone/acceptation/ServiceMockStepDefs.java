package fr.unice.polytech.codemara.drone.acceptation;

import fr.unice.polytech.codemara.drone.drone_service.DroneCommander;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.command.DeliveryCommand;
import fr.unice.polytech.codemara.drone.entities.command.InitCommand;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.CALLED_HOME;
import static junit.framework.TestCase.assertEquals;

/**
 * Here a regrouped all step defs relating to service mock, from initialization, to stubing, verification and tear down
 */
public class ServiceMockStepDefs {

    private IntegrationContext context = IntegrationContext.getInstance();

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DroneCommander droneCommander;

    private BlockingQueue<ConsumerRecord<String, String>> orderRecords;
    private BlockingQueue<ConsumerRecord<String, String>> droneRecords;


    @And("A mocked Order Service")
    public void aMockedOrderService() {
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(System.getProperty("spring.kafka.bootstrap-servers"),
                        "test", "false");
        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerProperties);
        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties("order-soon", "order-delivered", "order-cancelled");
        if (this.context.orderContainer != null) {
            this.context.orderContainer.stop();
        }
        // create a Kafka MessageListenerContainer
        this.context.orderContainer = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);
        // create a thread safe queue to store the received message
        orderRecords = new LinkedBlockingQueue<>();
        // setup a Kafka message listener
        this.context.orderContainer
                .setupMessageListener((MessageListener<String, String>) record -> orderRecords.add(record));
        // start the container and underlying message listener
        this.context.orderContainer.start();
        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(this.context.orderContainer, 3 * 2);
    }

    @Then("^The OrderService receives (\\d+) delivery notification$")
    public void theOrderServiceReceivesANotification(int notificationCount) throws InterruptedException {
        Thread.sleep(1000);
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        orderRecords.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        assertEquals(notificationCount, actual.size());
    }

    @And("A Mocked External Drone Commander")
    public void aMockedExternalDroneCommander() {
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(System.getProperty("spring.kafka.bootstrap-servers"),
                        "test", "false");
        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerProperties);
        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties("drone-commands");
        if (this.context.droneContainer != null) {
            this.context.droneContainer.stop();
        }
        // create a Kafka MessageListenerContainer
        this.context.droneContainer = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);
        // create a thread safe queue to store the received message
        droneRecords = new LinkedBlockingQueue<>();
        // setup a Kafka message listener
        this.context.droneContainer
                .setupMessageListener((MessageListener<String, String>) record -> droneRecords.add(record));
        // start the container and underlying message listener
        this.context.droneContainer.start();
        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(this.context.droneContainer, 2);
    }

    @Then("A delivery canceled notification is sent to the order service for each delivery")
    public void aDeliveryCanceledNotificationIsSentToTheOrderService() {
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        orderRecords.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        int notifiCount = 0;

        for (Delivery delivery : deliveryRepository.findAll()) {
            notifiCount++;
        }

        assertEquals(notifiCount, actual.size());
    }


    @Then("A Callback Command is Issued for all drones")
    public void aCallbackCommandIsIssuedForAllDrones() throws InterruptedException {
        int commandSend = 0;


        for (Drone drone : droneRepository.findAll()) {
            drone.setDroneStatus(CALLED_HOME);
            droneRepository.save(drone);
            commandSend++;
        }

        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        droneRecords.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        assertEquals(commandSend, actual.size());
    }

    @Then("A delivery command is sent to an available drone")
    public void aDeliveryCommandIsSentToAnAvailableDrone() throws InterruptedException {
        DeliveryCommand deliveryCommand = new DeliveryCommand(this.context.currentDrone,
                deliveryRepository.findByOrderIdAndItemId(this.context.currentDelivery.getOrderId(), this.context.currentDelivery.getItemId()));

        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        droneRecords.drainTo(received);
        this.droneCommander.sendCommand(deliveryCommand);

        Thread.sleep(10000);

        received = new ArrayList<>();
        droneRecords.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        assertEquals(1, actual.size());
    }

    @And("A Drone initialization command is sent to the drone")
    public void aDroneInitializationCommandIsSentToTheDrone() {
        Drone drone = droneRepository.findAll().iterator().next();
        long newId = drone.getDroneID();
        drone.setDroneID(this.context.currentDrone.getDroneID());
        InitCommand initializationCommand = new InitCommand(drone, newId);
        droneCommander.sendCommand(initializationCommand);

        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        droneRecords.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        assertEquals(1, actual.size());
    }

    @And("The mock server is teared down")
    public void theMockServerIsTearedDown() {
        System.out.println("teardown");
        if (this.context.orderContainer != null) {
            this.context.orderContainer.stop();
            if (orderRecords != null) orderRecords.clear();
        }
        if (this.context.droneContainer != null) {
            this.context.droneContainer.stop();
            if (droneRecords != null) droneRecords.clear();
        }
    }

    @And("A pause of {int} seconds")
    public void aPauseOfSeconds(int arg0) throws InterruptedException {
        Thread.sleep(arg0 * 1000);
    }
}
