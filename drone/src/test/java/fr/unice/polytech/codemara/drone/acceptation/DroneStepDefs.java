package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import fr.unice.polytech.codemara.drone.repositories.WhereaboutsRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Here are to be found the drone specific step defs, relating to data initilization, modification, and commands issuing
 * However drone state is handled in the {@link DroneStateStepDefs} class
 */
public class DroneStepDefs {


    @Autowired
    Environment environment;
    @Autowired
    DroneRepository droneRepository;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeliveryRepository deliveryRepository;

    private Drone activeDrone;

    @Autowired
    private WhereaboutsRepository whereaboutsRepository;
    private IntegrationContext context = IntegrationContext.getInstance();


    @Given("An active Drone Fleet")
    public void anActiveDroneFleet() {
        this.anEmptyFleet();
        List<Drone> drones = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Drone drone = new Drone(new Random().nextInt(100));
            drone.setDroneStatus(ACTIVE);
            drones.add(
                    drone
            );
        }

        droneRepository.saveAll(drones);
        this.context.currentDroneList = drones;
    }

    @And("a free drone")
    public void aFreeDrone() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone.setDroneStatus(DroneStatus.ACTIVE);
        drone.currentDelivery = null;
        drone = droneRepository.save(drone);
        this.context.currentDrone = drone;
    }

    @And("A sidelined drone")
    public void aSidelinedDrone() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone.setDroneStatus(ASIDE);
        drone.currentDelivery = null;
        drone = droneRepository.save(drone);
        this.context.currentDrone = drone;
    }

    @When("Elena callbacks the drones")
    public void elenaCallbacksTheDrones() throws Exception {
        mockMvc.perform(post("/drone/fleet/command/callback")).andExpect(status().isOk());
    }


    @And("All drones states is Callback")
    public void allDronesStatesIsCallback() {
        droneRepository.findAll().forEach(drone -> assertEquals(CALLED_HOME, drone.getDroneStatus()));
    }


    @When("Klaus requires a delivery")
    public void klausRequiresADelivery() throws Exception {
        Delivery test_delivery = new Delivery();
        test_delivery.setItemId(1);
        test_delivery.setOrderId(1);
        test_delivery.setPickup_location(new Location(10, 10));
        test_delivery.setTarget_location(new Location(11, 11));
        String test_delivery_json = new ObjectMapper().writeValueAsString(test_delivery);
        this.context.currentDelivery = test_delivery;
        MockHttpServletRequestBuilder put = post("/drone/request_delivery");
        put = put.contentType("application/json");
        mockMvc.perform(put.content(test_delivery_json)).andExpect(status().isOk());
    }

    @And("The sent delivery is registered")
    public void theSentDeliveryIsRegistered() {
        assertNotNull(deliveryRepository.findByOrderIdAndItemId(this.context.currentDelivery.getOrderId(), this.context.currentDelivery.getItemId()));
    }


    @And("deliveries in completion")
    public void deliveriesInCompletion() {
        for (long i = 1; i < 16; i++) {
            Optional<Drone> result = this.droneRepository.findById(i);
            Drone drone = result.get();
            Delivery delivery = new Delivery();
            delivery.setTarget_location(new Location(10, 10));
            delivery.setPickup_location(new Location(11, 11));
            delivery.setItemId(i);
            delivery.setOrderId(i);
            deliveryRepository.save(delivery);
            drone.currentDelivery = delivery;
            droneRepository.save(drone);
        }

    }


    @Given("An empty fleet")
    public void anEmptyFleet() {
        this.droneRepository.deleteAll(this.droneRepository.findAll());
    }

    @And("An empty DeliveryHistory")
    public void anEmptyDeliveryHistory() {
        this.deliveryRepository.deleteAll(this.deliveryRepository.findAll());
    }


    @Given("^A basic drone fleet$")
    public void aBasicDroneFleet() {
        this.anActiveDroneFleet();
    }


    @And("An active drone")
    public void anActiveDroneNamed() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone = droneRepository.save(drone);
        this.activeDrone = drone;
    }

    @When("Elena calls the active back to base")
    public void elenaCallsBackToBase() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + this.activeDrone.getDroneID() + "/" + DroneStatus.CALLED_HOME + "");
        mockMvc.perform(req).andExpect(status().isOk());
    }


    @Then("Drone active status is {string}")
    public void sStatusIs(String statusName) {
        DroneStatus status = DroneStatus.find(statusName).get();
        Iterable<Drone> drones = droneRepository.getDronesByDroneStatus(status);
        assertTrue(drones.iterator().hasNext());
        // assert drone has this status
    }


    @When("^Elena asks to set the drone aside$")
    public void elenaAsksToSetAside() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + this.context.currentDrone.getDroneID() + "/" + ASIDE + "");
        mockMvc.perform(req)
                .andExpect(status().isOk());
    }

    @Then("The drone's status is {string}")
    public void theDroneSStatusIs(String statusName) {
        this.context.currentDrone = droneRepository.findById(this.context.currentDrone.getDroneID()).get();
        DroneStatus status = DroneStatus.find(statusName).get();
        Assert.assertTrue(this.context.currentDrone.is(status));
    }

    @When("^Elena asks to set the drone ready for service$")
    public void elenaAsksToSetReadyForService() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + this.context.currentDrone.getDroneID() + "/" + ACTIVE + "");
        mockMvc.perform(req)
                .andExpect(status().isOk());
    }


    @Given("A delivering drone")
    public void aDeliveringDroneOfId() {
        this.context.currentDrone = new Drone();
        Delivery delivery = new Delivery();
        delivery.setTarget_location(new Location(45, 7));
        delivery.setPickup_location(new Location(45, 8));
        deliveryRepository.save(delivery);
        this.context.currentDrone.setCurrentDelivery(delivery);
        Whereabouts whereabouts = new Whereabouts(0, new Location(45, 7), 100, 300);
        whereaboutsRepository.save(whereabouts);
        this.context.currentDrone.setWhereabouts(whereabouts);
        this.context.currentDrone.setBatteryLevel(100);
        this.context.currentDrone.setDroneStatus(ACTIVE);
        droneRepository.save(context.currentDrone);

    }

    @Then("The drone is added in the database")
    public void theDroneIsAddedInTheDatabase() {
        Iterable<Drone> all = droneRepository.findAll();
        int count = 0;
        for (Drone drone :
                all) {
            count += 1;
        }
        assertEquals(1, count);
    }

    @And("The drone is assigned a new id")
    public void theDroneIsAssignedANewId() {
        Drone drone  = droneRepository.findAll().iterator().next();
        assertNotEquals(this.context.currentDrone.getDroneID(),drone.getDroneID());
    }
}
