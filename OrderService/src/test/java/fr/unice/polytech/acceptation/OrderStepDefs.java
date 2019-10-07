package fr.unice.polytech.acceptation;

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
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.io.UnsupportedEncodingException;

import static fr.unice.polytech.entities.NotificationMedium.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
public class OrderStepDefs extends SpringCucumberStepDef {

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

    private MvcResult last_query;

    private MockServerClient mockServer;
    private ClientAndServer clientServer;

    private Item item;
    private Customer customer;
    private Order order;

    private int orderId;
    private int itemId;
    private int customerId;

    private HttpRequest request;
    private RequestBuilder requestBuilder;
    private MvcResult result;

    private JsonElement jsonElement;

    /**
     * JSON helper method
     *
     * @param obj Object to parse as a Json String
     * @return The json string obtained
     */
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @And("The mock server is teared down")
    public void theMockServerIsTearedDown() {
        if (this.clientServer != null) this.clientServer.stop();
        this.mockServer = null;
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
        int serverPort = 20000;
        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);

        System.setProperty("WAREHOUSE_HOST", "http://localhost:20000");

        request = new HttpRequest();
        request.withMethod("POST").withPath("/warehouse/orders");
        mockServer.when(request).respond(response().withStatusCode(200));

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
    public void warehouseConfirmation() throws Exception {
        mockMvc.perform(post("/order")
                .content(jsonElement.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        mockServer.verify(request, VerificationTimes.atLeast(2));
        mockServer.close();
        clientServer.close();
    }

    @Given("^A drone with a client delivery$")
    public void setNotificationMock() {
        item = new Item("Persona 5");
        itemRepo.save(item);
        customer = new Customer("Roger", "Regor");
        customerRepo.save(customer);
        Coord coord = new Coord("0", "0");
        coordRepo.save(coord);
        order = new Order(coord, item, Status.PENDING, customer, "Bla bla bla");
        orderRepo.save(order);

        int serverPort = 20000;
        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);
        System.setProperty("NOTIFY_HOST", "http://localhost:20000");

        request = new HttpRequest();
        request.withMethod("POST").withPath("/notification/customer/" + order.getCustomer().getId() + "/order");
        mockServer.when(request).respond(response().withStatusCode(200));
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

        int serverPort = 20000;
        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);
        System.setProperty("NOTIFY_HOST", "http://localhost:20000");

        request = new HttpRequest();
        request.withMethod("POST").withPath("/notification/customer/" + order.getCustomer().getId() + "/order");
        mockServer.when(request).respond(response().withStatusCode(200));
    }

    @When("^Drone is cancel by fleet manager$")
    public void setCancelRequest() throws Exception {
        requestBuilder = get("/order/notify/cancel/" + order.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8);
        result = mockMvc.perform(requestBuilder).andReturn();
    }

    @Then("^A notification is send to client$")
    public void verifyCancelNotification() {
        mockServer.verify(request, VerificationTimes.atLeast(1));
    }

    @Given("^There are no customer$")
    public void thereAreNoCustomer() {
        customerRepo.deleteAll(customerRepo.findAll());
    }

    @Given("^A customer$")
    public void aCustomer() {
        customer = new Customer("Roger", "Regor");
        customerRepo.save(customer);
    }

    @When("The customer asks to be notified by {string}")
    public void theCustomerAsksToBeNotifiedByMedium(String mediumName) throws Exception {
        int serverPort = 20000;

        clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);

        request = new HttpRequest();
        request.withMethod("POST").withPath("/warehouse/orders");
        mockServer.when(request)
                .respond(response().withStatusCode(200));

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

}
