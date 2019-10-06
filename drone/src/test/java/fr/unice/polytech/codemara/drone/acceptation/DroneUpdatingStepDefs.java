package fr.unice.polytech.codemara.drone.acceptation;

import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.Fleet;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AutoConfigureMockMvc
public class DroneUpdatingStepDefs extends SpringCucumberStepDef {

    private Fleet fleet;
    private ResultActions last_query;
    private Drone drone;

    @Autowired
    private MockMvc mockMvc;

    @Given("^A drone called {string}$")
    public void aBasicDroneFleet(String droneId) {
        Random random = new Random();
        List<Drone> drones = new ArrayList<>();
        for (int i = 0; i < 15; i++) drones.add(new Drone("droneID" + i, random.nextInt(100)));
        this.fleet = new Fleet(drones);
    }

    @Given("A drone called {string}")
    public void aDroneCalled(String arg0) {

    }
}
