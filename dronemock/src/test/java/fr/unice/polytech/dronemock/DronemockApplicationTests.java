package fr.unice.polytech.dronemock;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.dronemock.models.Delivery;
import fr.unice.polytech.dronemock.models.Location;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DronemockApplicationTests {

    @ClassRule
    public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, "drones", "drones-commands", "drones-pickup", "drones-deliveries");

    private KafkaTemplate kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private MockMvc mockMvc;

    private static String RECEIVER_TOPIC = "drones-commands";

    @BeforeClass
    public static void beforeAll(){
        System.out.println("broker in the cucumber runner "+ rule.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("spring.kafka.bootstrap-servers",
                rule.getEmbeddedKafka().getBrokersAsString());
    }

    @Before
    public void setup() {
        // set up the Kafka producer properties
        Map<String, Object> senderProperties =
                KafkaTestUtils.senderProps(
                        rule.getEmbeddedKafka().getBrokersAsString());

        // create a Kafka producer factory
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        senderProperties);

        // create a Kafka template
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
        // set the default topic to send to
        kafkaTemplate.setDefaultTopic(RECEIVER_TOPIC);
        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
                .getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    rule.getEmbeddedKafka().getPartitionsPerTopic());
        }
    }

    @After
    public void cleanup() throws Exception {
        MvcResult a = mockMvc.perform(get("/commands/debug/reset")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
    }

    @Test
    public void contextLoads() {
        // Test id the application is launching properly
    }

    @Test
    @Ignore
    public void initCommandTest() throws Exception {
        String event = "{\"command\":\"INITIALISATION\", \"payload\":\"5\"}";
        this.kafkaTemplate.sendDefault(event);
        MvcResult a = mockMvc.perform(get("/commands/debug/id")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        long id = Long.parseLong(a.getResponse().getContentAsString());
        assertEquals(5, id);
    }


    @Test
    @Ignore
    public void deliveryCommandTest() throws Exception {
        Delivery delivery = new Delivery(5, 5, 5, new Location(7, 7), new Location(8, 8), true);
        String jsonDelivery = new ObjectMapper().writeValueAsString(delivery);
        String event = "{\"command\":\"DELIVERY\", \"payload\":" + jsonDelivery + "}";
        this.kafkaTemplate.sendDefault(event);
        MvcResult a = mockMvc.perform(get("/commands/debug/delivery")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Delivery received = new ObjectMapper().readValue(a.getResponse().getContentAsString(), Delivery.class);
        assertEquals(delivery, received);
    }


    @Test
    public void callbackCommandTest() throws Exception {
        Location base = new Location(5, 5);
        String jsonBase = new ObjectMapper().writeValueAsString(base);
        String event = "{\"command\":\"CALLBACK\", \"payload\":" + jsonBase + "}";
        this.kafkaTemplate.sendDefault(event);
        MvcResult a = mockMvc.perform(get("/commands/debug/base")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Location received = new ObjectMapper().readValue(a.getResponse().getContentAsString(), Location.class);
        assertEquals(base, received);
    }







}
