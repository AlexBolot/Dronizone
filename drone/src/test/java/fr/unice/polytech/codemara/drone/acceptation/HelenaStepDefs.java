package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HelenaStepDefs {
    @Autowired
    private MockMvc mockMvc;

    private ResultActions last_query;

    @When("Elena wants to know the battery levels of the fleet")
    public void elenaWantsToKnowTheBatteryLevelsOfTheFleet() throws Exception {
        MockHttpServletRequestBuilder req = get("/drone/fleet_battery_status");
        this.last_query = mockMvc.perform(req)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Then("She receives the list of every drone and their battery level")
    public void sheReceivesTheListOfEveryDroneAndTheirBatteryLevel() throws IOException {
        MvcResult result = this.last_query.andReturn();
        String body = result.getResponse().getContentAsString();
        ObjectNode jsonNode = new ObjectMapper().readValue(body, ObjectNode.class);

        ArrayList<Double> batteryLevels = new ArrayList<>();
        jsonNode.elements().forEachRemaining(element -> batteryLevels.add(element.asDouble()));

        Assert.assertEquals(15, batteryLevels.size());
    }

}
