package fr.unice.polytech.codemera.watchdog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemera.watchdog.entities.Order;
import fr.unice.polytech.codemera.watchdog.repositories.OrderRepo;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static fr.unice.polytech.codemera.watchdog.entities.Order.OrderStatus.PENDING;
import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WatchDogApplicationTests {

    @ClassRule
    public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, "order-create");

    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private MockMvc mockMvc;

    private MvcResult last_query;

    private MockServerClient mockServer;
    private ClientAndServer clientServer;

    private HttpRequest request;

    private static String RECEIVER_TOPIC = "order-create";

    @BeforeClass
    public static void beforeAll() {
        System.out.println("broker in the cucumber runner " + rule.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("spring.kafka.bootstrap-servers",
                rule.getEmbeddedKafka().getBrokersAsString());
    }

    @Before
    public void setup() throws JsonProcessingException {
        // set up the Kafka producer properties
        Map<String, Object> senderProperties = KafkaTestUtils.senderProps(rule.getEmbeddedKafka().getBrokersAsString());

        // create a Kafka producer factory
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(senderProperties);

        // create a Kafka template
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
        // set the default topic to send to
        kafkaTemplate.setDefaultTopic(RECEIVER_TOPIC);
        // wait until the partitions are assigned
        for (MessageListenerContainer listener : kafkaListenerEndpointRegistry.getListenerContainers()) {
            waitForAssignment(listener, rule.getEmbeddedKafka().getPartitionsPerTopic());
        }

        int serverPort = 20000;
        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);

        System.setProperty("NOTIFY_HOST", "http://localhost:20000");

        request = new HttpRequest();

        Map<String, String> params = new HashMap<>();
        params.put("target_id", String.valueOf(3));
        params.put("order_id", String.valueOf(180));
        params.put("payload", "I detected a strange customer behavior !!");

        request.withMethod("POST").withPath("/notification/alert/").withBody(new ObjectMapper().writeValueAsString(params));
        mockServer.when(request).respond(response().withStatusCode(200));
    }

    @After
    public void cleanup() throws Exception {
        orderRepo.deleteAll();
        if (this.clientServer != null) this.clientServer.stop();
        this.mockServer = null;
    }

    @Test
    public void contextLoads() {
        // Test if the application is launching properly
    }

    @Test
    public void testAlert() throws InterruptedException {

        for (int i = 0; i < 20; i++) {
            long timeStamp = Timestamp.valueOf(now()).getTime();
            Order order = new Order(i, 3, PENDING, timeStamp);
            orderRepo.save(order);
        }

        long timeStamp = Timestamp.valueOf(now()).getTime();
        String event = "{\"order_id\":" + 180 + ", \"timestamp\":" + timeStamp + ", \"orderStatus\":\"PENDING\", \"payload\":{\"customer_id\":3}}";
        this.kafkaTemplate.sendDefault(event);

        Thread.sleep(1000);
        mockServer.verify(request, VerificationTimes.atLeast(1));
    }

    /**
     * Testing that
     */
    @Test
    public void testRAS() throws InterruptedException {

        for (int i = 0; i < 19; i++) {
            long timeStamp = Timestamp.valueOf(now()).getTime();
            Order order = new Order(i, 3, PENDING, timeStamp);
            orderRepo.save(order);
        }

        assertEquals(18, orderRepo.count());

        long timeStamp = Timestamp.valueOf(now()).getTime();
        String event = "{\"order_id\":" + 180 + ", \"timestamp\":" + timeStamp + ", \"orderStatus\":\"PENDING\", \"payload\":{\"customer_id\":3}}";
        this.kafkaTemplate.sendDefault(event);

        Thread.sleep(1000);
        mockServer.verify(request, VerificationTimes.exactly(0));

        assertEquals(19, orderRepo.count());
    }

    /**
     * Testing that a too old order is deleted when adding a new one
     */
    @Test
    public void delete() throws InterruptedException {

        long timeStamp = Timestamp.valueOf(now().minusMinutes(5)).getTime();
        Order order = new Order(1, 3, PENDING, timeStamp);
        orderRepo.save(order);

        assertEquals(1, orderRepo.count());

        timeStamp = Timestamp.valueOf(now()).getTime();
        String event = "{\"order_id\":" + 180 + ", \"timestamp\":" + timeStamp + ", \"orderStatus\":\"PENDING\", \"payload\":{\"customer_id\":3}}";
        this.kafkaTemplate.sendDefault(event);

        Thread.sleep(1000);

        assertEquals(1, orderRepo.count());
    }
}