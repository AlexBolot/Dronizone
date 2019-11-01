package fr.unice.polytech.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.entities.*;
import fr.unice.polytech.repo.CoordRepo;
import fr.unice.polytech.repo.CustomerRepo;
import fr.unice.polytech.repo.ItemRepo;
import fr.unice.polytech.repo.OrderRepo;
import gherkin.deps.com.google.gson.JsonElement;
import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static fr.unice.polytech.entities.NotificationMedium.valueOf;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
public class OrderStepDefs {

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

    @And("The mock server is teared down")
    public void theMockServerIsTearedDown() {
        if (this.clientServer != null) this.clientServer.stop();
        this.mockServer = null;
        if (warehouseContainer != null) warehouseContainer.stop();
        if (warehouseRecords != null) warehouseRecords.clear();
    }

    @Given("^An Item and the client information$")
    public void setupItemAndCustomer() {
        item = new Item("Persona 5");
        customer = new Customer("Roger", "Regor");
    }

    @When("^The client will order this Item$")
    public void orderItem() {
        order = new Order(new Coord("0", "0"), item, null, customer, "bla bla");
    }

    @Then("^The client will receive the order as confirmation$")
    public void passOrder() throws Exception {
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(System.getProperty("spring.kafka.bootstrap-servers"),
                        "test", "false");
        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerProperties);
        // set the topic that needs to be consumed
        ContainerProperties containerProperties = new ContainerProperties("order-create");
        if (this.warehouseContainer != null) {
            this.warehouseContainer.stop();
        }
        // create a Kafka MessageListenerContainer
        this.warehouseContainer = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);
        // create a thread safe queue to store the received message
        warehouseRecords = new LinkedBlockingQueue<>();
        // setup a Kafka message listener
        this.warehouseContainer.setupMessageListener((MessageListener<String, String>) record -> warehouseRecords.add(record));
        // start the container and underlying message listener
        this.warehouseContainer.start();
        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(this.warehouseContainer, 2);

        JsonParser parser = new JsonParser();

        jsonElement = parser.parse("{\"id\": \"1\",\"jsonrpc\": \"2.0\",\"method\": \"orderItem\"}");
        JsonObject requestOrder = new JsonObject();
        JsonObject requestCoord = new JsonObject();
        requestCoord.addProperty("lat", order.getCoord().getLat());
        requestCoord.addProperty("lon", order.getCoord().getLon());
        JsonObject requestItem = new JsonObject();
        requestItem.addProperty("name", item.getName());
        JsonObject requestCustomer = new JsonObject();
        requestCustomer.addProperty("name", customer.getName());
        requestCustomer.addProperty("firstName", customer.getFirstName());
        requestOrder.add("coord", requestCoord);
        requestOrder.add("item", requestItem);
        requestOrder.add("customer", requestCustomer);
        requestOrder.addProperty("paymentInfo", order.getPaymentInfo());
        JsonObject param = new JsonObject();
        param.add("order", requestOrder);

        jsonElement.getAsJsonObject().add("params", param);

        this.last_query = mockMvc.perform(post("/order")
                .content(jsonElement.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        assertNotNull(last_query.getResponse().getContentAsString());

        JsonElement responseContent = parser.parse(last_query.getResponse().getContentAsString());
        JsonElement jsonOrder = responseContent.getAsJsonObject().get("result");
        String result = jsonOrder.getAsJsonObject().get("status").getAsString();
        orderId = jsonOrder.getAsJsonObject().get("id").getAsInt();
        itemId = jsonOrder.getAsJsonObject().get("item").getAsJsonObject().get("id").getAsInt();
        customerId = jsonOrder.getAsJsonObject().get("customer").getAsJsonObject().get("id").getAsInt();
        assertEquals("PENDING", result);
    }

    @And("^The Warehouse service will receive the order$")
    public void warehouseConfirmation() {
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        warehouseRecords.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());

        assertEquals(1, actual.size());
        warehouseContainer.stop();
        warehouseRecords.clear();
    }

    @Given("^A drone with a client delivery$")
    public void setNotificationMock() throws JsonProcessingException, InterruptedException {
        item = new Item("Persona 5");
        itemRepo.save(item);
        customer = new Customer("Roger", "Regor");
        customerRepo.save(customer);
        Coord coord = new Coord("0", "0");
        coordRepo.save(coord);
        order = new Order(coord, item, Status.PENDING, customer, "Bla bla bla");
        orderRepo.save(order);

        request = new HttpRequest();

        request.withMethod("POST").withPath("/notifications/customer/" + order.getCustomer().getId() + "/order");
        int serverPort = 20000;
        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);
        mockServer.when(request).respond(response().withStatusCode(200));
        System.setProperty("NOTIFY_HOST", "http://localhost:20000");

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
        kafkaTemplate.setDefaultTopic("order-soon");
        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry
                .getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    Integer.parseInt(System.getProperty("spring.kafka.partitions-per-topics")));
        }

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", order.getId());

        kafkaTemplate.send("order-soon", new ObjectMapper().writeValueAsString(params));

        Thread.sleep(10000);

        mockServer.verify(request, VerificationTimes.atLeast(1));
    }

    @When("^The drone is near his delivery location$")
    public void setRequest() {
        requestBuilder = get("/order/notify/delivery/" + order.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8);
    }

    @Then("^The drone send a notification to Order service$")
    public void sendRequest() throws Exception {
        result = mockMvc.perform(requestBuilder).andReturn();
    }

    @And("^The client receives the notification that their delivery is close by$")
    public void verifyMockServer() {
        mockServer.verify(request, VerificationTimes.atLeast(1));
        mockServer.close();
        clientServer.close();
    }

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
        request.withMethod("POST").withPath("/notifications/customer/" + order.getCustomer().getId() + "/order");
        int serverPort = 20000;
        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);
        mockServer.when(request).respond(response().withStatusCode(200));
        System.setProperty("NOTIFY_HOST", "http://localhost:20000");
    }

    @When("^Drone is canceled by fleet manager$")
    public void setCanceledRequest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", order.getId());
        this.kafkaTemplate.send("order-cancelled", new ObjectMapper().writeValueAsString(params));
    }

    @Then("^A notification is send to client$")
    public void verifyCancelNotification() throws InterruptedException {
        Thread.sleep(10000);
        mockServer.verify(request, VerificationTimes.atLeast(1));
    }

    @Given("^There are no customer$")
    public void thereAreNoCustomer() {
        customerRepo.deleteAll(customerRepo.findAll());
        customerCount = (int) customerRepo.count();
    }

    @Given("^A customer$")
    public void aCustomer() {
        customer = new Customer("Roger", "Regor");
        customerRepo.save(customer);
    }

    @When("The customer asks to be notified by {string}")
    public void theCustomerAsksToBeNotifiedByMedium(String mediumName) throws Exception {
        JsonParser parser = new JsonParser();
        jsonElement = parser.parse("{\"id\": \"1\",\"jsonrpc\": \"2.0\",\"method\": \"setPersonalPreferences\"}");

        JsonObject param = new JsonObject();
        param.addProperty("customerId", customer.getId().toString());
        param.addProperty("notificationPreference", valueOf(mediumName).name());

        jsonElement.getAsJsonObject().add("params", param);

        this.last_query = mockMvc.perform(post("/order")
                .content(jsonElement.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        assertNotNull(last_query.getResponse().getContentAsString());
    }

    @Then("The client will receive {string} as confirmation")
    public void theClientWillReceiveAsConfirmation(String mediumName) throws UnsupportedEncodingException {
        JsonElement responseContent = new JsonParser().parse(last_query.getResponse().getContentAsString());
        assertEquals(mediumName, responseContent.getAsJsonObject().get("result").getAsString());
    }

    @Then("His notification medium is set to {string}")
    public void hisNotificationMediumIsSetTo(String mediumName) {
        customer = customerRepo.updateFrom(this.customer);
        assertEquals(valueOf(mediumName), customer.getMedium());
    }

    @When("Roger provides his identity")
    public void rogerProvidesHisIdentity() throws Exception {
        JsonParser parser = new JsonParser();
        jsonElement = parser.parse("{\"id\": \"1\",\"jsonrpc\": \"2.0\",\"method\": \"registerCustomer\"}");

        JsonObject param = new JsonObject();
        param.addProperty("firstName", "Roger");
        param.addProperty("name", "Regor");

        jsonElement.getAsJsonObject().add("params", param);

        this.last_query = mockMvc.perform(post("/order")
                .content(jsonElement.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        assertNotNull(last_query.getResponse().getContentAsString());
    }

    @Then("A customer is created")
    public void aCustomerIsCreated() throws UnsupportedEncodingException {
        JsonElement responseContent = new JsonParser().parse(last_query.getResponse().getContentAsString());
        int customerId = responseContent.getAsJsonObject().get("result").getAsInt();

        assertEquals(customerCount + 1, customerRepo.count());
        Optional<Customer> optCustomer = customerRepo.findById(customerId);

        assertTrue(optCustomer.isPresent());
        Customer customer = optCustomer.get();

        assertEquals("Roger", customer.getFirstName());
        assertEquals("Regor", customer.getName());
    }
}
