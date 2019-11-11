package fr.unice.polytech.codemara.drone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.drone_service.DroneCommander;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.Location;
import fr.unice.polytech.codemara.drone.entities.command.CallbackCommand;
import fr.unice.polytech.codemara.drone.entities.command.InitCommand;
import fr.unice.polytech.codemara.drone.entities.dto.DeliveryStatus;
import fr.unice.polytech.codemara.drone.entities.dto.DeliveryUpdateDTO;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import kafka.server.KafkaServer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class DroneApplicationTests {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DroneApplicationTests.class);

    @Autowired
    private DroneCommander droneCommander;


    private KafkaMessageListenerContainer<String, String> container;

    private BlockingQueue<ConsumerRecord<String, String>> records;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka =
            new EmbeddedKafkaRule(1, true, 1, "drones-commands", "drones-status", "drone-delivery-update", "order-delivered", "order-cancelled", "order-soon", "order-packed");
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private DroneRepository droneRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;

    @BeforeClass
    public static void beforeAll() {
        for (KafkaServer kafkaServer : embeddedKafka.getEmbeddedKafka().getKafkaServers()) {
//            kafkaServer.startup();

        }

    }

    @AfterClass
    public static void afterAll() {
        for (KafkaServer kafkaServer : embeddedKafka.getEmbeddedKafka().getKafkaServers()) {
//            kafkaServer.awaitShutdown();
        }
    }
    @Before
    public void setUp() throws Exception {
        // set up the Kafka consumer properties
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps("test", "false",
                        embeddedKafka.getEmbeddedKafka());

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<String, String>(
                        consumerProperties);

        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties("drone-commands");

        // create a Kafka MessageListenerContainer
        container = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>();

        // setup a Kafka message listener
        container
                .setupMessageListener(new MessageListener<String, String>() {
                    @Override
                    public void onMessage(
                            ConsumerRecord<String, String> record) {
                        LOGGER.debug("test-listener received message='{" + record.toString() + "}");
                        records.add(record);
                    }
                });

        // start the container and underlying message listener
        container.start();

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
        Map<String, Object> senderProperties =
                KafkaTestUtils.senderProps(
                        embeddedKafka.getEmbeddedKafka().getBrokersAsString());

        // create a Kafka producer factory
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        senderProperties);

        // create a Kafka template
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @After
    public void tearDown() {
        // stop the container
        container.stop();

    }


    @Test
    public void testSend() throws InterruptedException {
        // send the message
        Drone drone = new Drone(5);
        InitCommand command = new InitCommand(drone, 5);
        droneCommander.sendCommand(command);

        ConsumerRecord<String, String> message = records.poll(2, TimeUnit.SECONDS);
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        if (message != null)
            received.add(message);
        records.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        assertEquals(1, actual.size());
    }


    @Test
    public void testDeliveredUpdate() throws JsonProcessingException, InterruptedException {
        long orderId = 1;
        long itemId = 1;
        Drone drone = new Drone(100);

        Delivery currentDelivery = new Delivery();
        currentDelivery.setOrderId(orderId);
        currentDelivery.setTarget_location(new Location(45, 7));
        currentDelivery.setPickup_location(new Location(45, 7.5));
        deliveryRepository.save(currentDelivery);
        drone.setCurrentDelivery(currentDelivery);
        drone = droneRepository.save(drone);
        long droneId = drone.getDroneID();

        DeliveryUpdateDTO delivered = new DeliveryUpdateDTO(droneId, orderId, itemId, DeliveryStatus.DELIVERED);
        kafkaTemplate.send("drone-delivery-update", new ObjectMapper().writeValueAsString(delivered));

        ConsumerRecord<String, String> message = records.poll(10, TimeUnit.SECONDS);
        drone = droneRepository.findById(droneId).orElseThrow(IllegalStateException::new);
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        CallbackCommand callhomeCommand = new CallbackCommand(new Location(45, 7));
        callhomeCommand.setTarget(drone);
        if (message != null) {
            records.put(message);
        }
        records.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        String expected = new ObjectMapper().writeValueAsString(callhomeCommand);
        assertTrue(actual.toString() + " Should contain " + expected, actual.contains(expected));
        assertNull(drone.getCurrentDelivery());
    }


}
