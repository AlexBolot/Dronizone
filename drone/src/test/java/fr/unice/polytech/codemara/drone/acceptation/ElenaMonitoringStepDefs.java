package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.Drone.Status;
import fr.unice.polytech.codemara.drone.entities.Fleet;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ElenaMonitoringStepDefs extends SpringCucumberStepDef {

    private Fleet fleet;
    private ResultActions last_query;
    private Drone drone;

    @Autowired
    private MockMvc mockMvc;

    @Given("^A basic drone fleet$")
    public void aBasicDroneFleet() {
        Random random = new Random();
        List<Drone> drones = new ArrayList<>();
        for (int i = 0; i < 15; i++) drones.add(new Drone("droneID" + i, random.nextInt(100)));
        this.fleet = new Fleet(drones);
    }

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
        jsonNode.elements().forEachRemaining(element -> batteryLevels.add(Double.parseDouble(element.textValue())));

        Assert.assertEquals(15, batteryLevels.size());
    }

    @And("An active drone named {string}")
    public void anActiveDroneNamed(String droneId) {
        Drone drone = new Drone(droneId, new Random().nextInt(100));

    }

    @When("Elena calls {string} back to base")
    public void elenaCallsBackToBase(String droneId) throws Exception {
        MockHttpServletRequestBuilder req = get("/set_drone_aside/" + droneId + "/" + Status.CALLED_HOME + "");
        this.last_query = mockMvc.perform(req)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }


    @Then("{string}'s status is {string}")
    public void sStatusIs(String droneId, String statusName) {
        Status status = Status.find(statusName).get();

        // assert drone has this status
    }
}
