package fr.unice.polytech.codemara.warehouse.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.warehouse.entities.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.repositories.OrderRepository;

import gherkin.deps.com.google.gson.Gson;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class WarehouseStepDefs extends SpringCucumberStepDef {
    List<CustomerOrder> orders;
    private ResultActions last_query;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    Environment environment;
    private MockServerClient mockServer;
    private ClientAndServer clientServer;

    private static final Logger logger = LoggerFactory.getLogger(WarehouseStepDefs.class);

    @Given("^A basic order list$")
    public void aBasicOrderList() {
        logger.info("######################## Soit une liste de commandes simples ########################");
        orders = Arrays.asList(
                new CustomerOrder(),
                new CustomerOrder(),
                new CustomerOrder()
        );
        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).setItem_id(i);
            orders.get(i).setCustomer_id(i);
            orders.get(i).setLat(String.valueOf(i) + "S");
            orders.get(i).setLon(String.valueOf(i) + "E");
            orders.get(i).setStatus(CustomerOrder.OrderStatus.PENDING);
            orderRepository.save(orders.get(i));
        }

    }

    @Autowired
    private MockMvc mockMvc;

    @When("^Klaus queries the pending dispatch list$")
    public void klausQueriesThePendingDispathList() throws Exception {
        logger.info("######################## Quand Klauss demande les listes des commandes a preparer ########################");
        this.last_query = mockMvc.perform(get("/warehouse/orders"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Then("^The client receives a (\\d+) status code$")
    public void theClientReceivesAStatusCode(int expected_status) throws Exception {
        logger.info("######################## le code client recoit un status HTTP 200 OK ########################");
        this.last_query.andExpect(status().is(expected_status));
    }

    @And("^The client receives the basic order list$")
    public void theClientReceivesTheBasicOrderList() throws UnsupportedEncodingException, JsonProcessingException {
        logger.info("######################## et le code client recoit la liste de commandes a preparer. ########################");
        MvcResult result = this.last_query.andReturn();
        String body = result.getResponse().getContentAsString();
        assertNotEquals("[]", body);
        ObjectMapper jsonMapper = new ObjectMapper();
        String expectedJson = jsonMapper.writeValueAsString(this.orders);
        assertEquals(expectedJson, body);
    }

    @And("A mocked drone server")
    public void aMockedDroneServer() {
        logger.info("######################## et un serveur de drone mocke. ########################");
        int serverPort = 20000;
        System.setProperty("DRONE_HOST", "http://localhost:20000/");
        this.clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);

        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("Pickup on its way")
                );
        ;
    }

    @When("Klaus sets a query ready for delivery")
    public void klausSetsAQueryReadyForDelivery() throws Exception {
        logger.info("######################## Quand Klaus definit une commande comme prete a la livraison ########################" );
        this.last_query = mockMvc.perform(put("/warehouse/orders/1"));
    }

    @And("The mock drone server receives a post query")
    public void theMockDroneServerReceivesAPostQuery() {
        logger.info("######################## et le serveur de drone mocke recoit une requete post. ########################");
        this.mockServer.verify(
                request()
                        .withPath("/"),
                VerificationTimes.once()
        );
    }
}
