package fr.unice.polytech.acceptation;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.entities.*;
import fr.unice.polytech.repo.CoordRepo;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import gherkin.deps.com.google.gson.JsonElement;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;

public class OrderNotificationStepDefs {

    @Autowired
    private Environment environment;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private CoordRepo coordRepo;

    @Autowired
    private MockMvc mockMvc;

    private KafkaTemplate kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private MvcResult last_query;

    private MockServerClient mockServer;
    private ClientAndServer clientServer;

    private Item item;
    private Customer customer;
    private Order order;

    private int orderId;
    private int itemId;
    private int customerId;

    private int customerCount;

    private HttpRequest request;
    private RequestBuilder requestBuilder;
    private MvcResult result;

    private KafkaMessageListenerContainer<String, String> warehouseContainer;
    private BlockingQueue<ConsumerRecord<String, String>> warehouseRecords;

    private JsonElement jsonElement;

    @Given("^Bad weather forecast$")
    public void setMock() {
        item = new Item("Persona 5");
        itemRepo.save(item);
        customer = new Customer("Roger", "Regor");
        customerRepo.save(customer);
        Coord coord = new Coord("0", "0");
        coordRepo.save(coord);
        order = new Order(coord, item, Status.PENDING, customer, "Bla bla bla");
        orderRepo.save(order);

        // set up the Kafka producer properties
        Map<String, Object> senderProperties =
                KafkaTestUtils.senderProps(System.getProperty("spring.kafka.bootstrap-servers"));

        // create a Kafka producer factory
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        senderProperties);

        // create a Kafka template
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
        // set the default topic to send to
        kafkaTemplate.setDefaultTopic("order-cancelled");
        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
                .getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    Integer.parseInt(System.getProperty("spring.kafka.partitions-per-topics")));
        }

        request = new HttpRequest();
        request.withMethod("POST").withPath("/notification/customer/" + order.getCustomer().getId() + "/order");
        int serverPort = 2000;
        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);
        mockServer.when(request).respond(response().withStatusCode(200));
        System.setProperty("NOTIFY_HOST", "http://localhost:2000");
    }

    @When("^Drone is cancel by fleet manager$")
    public void setCancelRequest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", order.getId());
        this.kafkaTemplate.send("order-cancelled", new ObjectMapper().writeValueAsString(params));
    }

    @Then("^A notification is send to client$")
    public void verifyCancelNotification() {
        mockServer.verify(request, VerificationTimes.atLeast(1));
    }
    
}
