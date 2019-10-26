package fr.unice.polytech.codemara.drone;

import fr.unice.polytech.codemara.drone.drone_service.DroneCommander;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.command.InitCommand;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.apache.kafka.common.config.ConfigResource.Type.TOPIC;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@DirtiesContext
public class DroneApplicationTests {

//    private static final Logger LOGGER =
//            LoggerFactory.getLogger(DroneApplicationTests.class);
//
//    @Autowired
//    private DroneCommander droneCommander;
//
//
//    private KafkaMessageListenerContainer<String, String> container;
//
//    private BlockingQueue<ConsumerRecord<String, String>> records;
//
//    @ClassRule
//    public static EmbeddedKafkaRule embeddedKafka =
//            new EmbeddedKafkaRule(1, true, "drones-commands", "drones-status", "drone-delivery-update", "order-delivered", "order-cancelled", "order-soon", "order-packed");
//
//    @Before
//    public void setUp() throws Exception {
//        // set up the Kafka consumer properties
//        Map<String, Object> consumerProperties =
//                KafkaTestUtils.consumerProps("test", "false",
//                        embeddedKafka.getEmbeddedKafka());
//
//        // create a Kafka consumer factory
//        DefaultKafkaConsumerFactory<String, String> consumerFactory =
//                new DefaultKafkaConsumerFactory<String, String>(
//                        consumerProperties);
//
//        // set the topic that needs to be consumed
//        ContainerProperties containerProperties =
//                new ContainerProperties("drone-commands");
//
//        // create a Kafka MessageListenerContainer
//        container = new KafkaMessageListenerContainer<>(consumerFactory,
//                containerProperties);
//
//        // create a thread safe queue to store the received message
//        records = new LinkedBlockingQueue<>();
//
//        // setup a Kafka message listener
//        container
//                .setupMessageListener(new MessageListener<String, String>() {
//                    @Override
//                    public void onMessage(
//                            ConsumerRecord<String, String> record) {
//                        LOGGER.debug("test-listener received message='{" + record.toString() + "}");
//                        records.add(record);
//                    }
//                });
//
//        // start the container and underlying message listener
//        container.start();
//
//        // wait until the container has the required number of assigned partitions
//        ContainerTestUtils.waitForAssignment(container, 1);
//    }
//
//    @After
//    public void tearDown() {
//        // stop the container
//        container.stop();
//    }
//
//    @Test
//    public void testSend() throws InterruptedException {
//        // send the message
//        Drone drone = new Drone(5);
//        InitCommand command = new InitCommand(drone, 5);
//        droneCommander.sendCommand(command);
//
//        Thread.sleep(10000);
//
//        List<ConsumerRecord<String, String>> received = new ArrayList<>();
//        records.drainTo(received);
//        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
//        assertEquals(1, actual.size());
//    }
//
//    @Test
//    public void contextLoad() {
//        // Test the context load properly
//    }
}
