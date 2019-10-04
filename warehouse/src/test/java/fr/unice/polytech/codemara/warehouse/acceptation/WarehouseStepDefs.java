package fr.unice.polytech.codemara.warehouse.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.warehouse.entities.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.repositories.OrderRepository;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class WarehouseStepDefs extends SpringCucumberStepDef {
    List<CustomerOrder> orders;
    private ResultActions last_query;
    @Autowired
    OrderRepository orderRepository;

    @Given("^A basic order list$")
    public void aBasicOrderList() {
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
        this.last_query = mockMvc.perform(get("/warehouse/orders"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Then("^The client receives a (\\d+) status code$")
    public void theClientReceivesAStatusCode(int arg0) throws Exception {
        this.last_query.andExpect(status().isOk());
    }

    @And("^The client receives the basic order list$")
    public void theClientReceivesTheBasicOrderList() throws UnsupportedEncodingException, JsonProcessingException {
        MvcResult result = this.last_query.andReturn();
        String body = result.getResponse().getContentAsString();
        assertNotEquals("[]", body);
        ObjectMapper jsonMapper = new ObjectMapper();
        String expectedJson = jsonMapper.writeValueAsString(this.orders);
        assertEquals(expectedJson,body);
    }

}
