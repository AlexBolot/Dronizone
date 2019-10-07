package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.Fleet;
import fr.unice.polytech.codemara.drone.entities.Location;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.entities.command.CommandType;
import fr.unice.polytech.codemara.drone.entities.command.DeliveryCommand;
import fr.unice.polytech.codemara.drone.entities.command.DroneCommand;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import fr.unice.polytech.codemara.drone.repositories.WhereaboutsRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class DroneStepDefs extends SpringCucumberStepDef {


    @Autowired
    Environment environment;
    @Autowired
    DroneRepository droneRepository;
    @Autowired
    private MockMvc mockMvc;
    private MockServerClient mockServer;
    private ClientAndServer clientServer;
    private ResultActions last_response;
    private Delivery last_delivery;
    private Drone free_drone;
    @Autowired
    private DeliveryRepository deliveryRepository;
    private Drone activeDrone;


    @Autowired
    private WhereaboutsRepository whereaboutsRepository;

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
    }

    @And("a free drone")
    public void aFreeDrone() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone.setDroneStatus(DroneStatus.ACTIVE);
        drone.currentDelivery = null;
        drone = droneRepository.save(drone);
        this.free_drone = drone;
    }

    @And("A sidelined drone")
    public void aSidelinedDrone() {
        Drone drone = new Drone(new Random().nextInt(100));
        drone.setDroneStatus(ASIDE);
        drone.currentDelivery = null;
        drone = droneRepository.save(drone);
        this.free_drone = drone;
    }

    @And("A Mocked External Drone Commander")
    public void aMockedExternalDroneCommander() {
        int serverPort = 20000;
        System.setProperty("EXTERNAL_DRONE_HOST", "http://localhost:" + serverPort + "/");
        if (this.clientServer == null) {
            serverPort = 20000;
            this.clientServer = startClientAndServer(serverPort);
            mockServer = new MockServerClient("localhost", serverPort);
        }
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/commands")
                )
                .respond(
                        httpRequest -> {
                            try {
                                JsonNode command = new ObjectMapper().readTree(httpRequest.getBodyAsString());
                                if (command.path("type").textValue().equals("CALLBACK")) {
                                    mockMvc.perform(put("/drone/set_drone_aside/" + command.path("target").path("droneID") + "/" + CALLED_HOME)).andExpect(status().isOk());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return response("");
                        }
                );
    }

    @And("A mocked Order Service")
    public void aMockedOrderService() {
        int serverPort = 20000;
        System.setProperty("ORDER_SERVICE_HOST", "http://localhost:" + serverPort + "/");
        if (this.clientServer == null) {
            this.clientServer = startClientAndServer(serverPort);
            mockServer = new MockServerClient("localhost", serverPort);
        }
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/order/notify/cancel/.*")
                ).respond(response(""));
        ;
    }


    @When("Elena callbacks the drones")
    public void elenaCallbacksTheDrones() throws Exception {
        mockMvc.perform(post("/drone/fleet/command/callback")).andExpect(status().isOk());
    }

    @Then("A Callback Command is Issued for all drones")
    public void aCallbackCommandIsIssuedForAllDrones() throws JsonProcessingException {
        for (Drone drone :
                droneRepository.findAll()) {
            ObjectMapper mapper = new ObjectMapper();
            drone.setDroneStatus(ACTIVE);
            String droneCallbackJson = mapper.writeValueAsString(new DroneCommand(CommandType.CALLBACK).copyWith(drone));
            this.mockServer.verify(
                    request()
                            .withPath("/commands")

            );
        }

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
        this.last_delivery = test_delivery;
        MockHttpServletRequestBuilder put = post("/drone/request_delivery");
        put = put.contentType("application/json");
        mockMvc.perform(put.content(test_delivery_json)).andExpect(status().isOk());
    }

    @Then("A delivery command is sent to an available drone")
    public void aDeliveryCommandIsSentToAnAvailableDrone() throws JsonProcessingException {
        DeliveryCommand deliveryCommand = new DeliveryCommand();
        deliveryCommand.setDelivery(deliveryRepository.findByOrderIdAndItemId(this.last_delivery.getOrderId(), this.last_delivery.getItemId()));
        deliveryCommand.setTarget(this.free_drone);
        this.mockServer.verify(
                request().withPath("/commands").withBody(new ObjectMapper().writeValueAsString(deliveryCommand))
        );
    }

    @And("The sent delivery is registered")
    public void theSentDeliveryIsRegistered() {
        assertNotNull(deliveryRepository.findByOrderIdAndItemId(this.last_delivery.getOrderId(), this.last_delivery.getItemId()));
    }


    @Then("A delivery canceled notification is sent to the order service")
    public void aDeliveryCanceledNotificationIsSentToTheOrderService() {

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


    @And("The mock server is teared down")
    public void theMockServerIsTearedDown() {
        System.out.println("teardown");
        if (this.clientServer != null) {
            this.clientServer.stop();
        }
        this.mockServer = null;
    }

    @Given("An empty fleet")
    public void anEmptyFleet() {
        this.droneRepository.deleteAll(this.droneRepository.findAll());
    }

    @And("An empty DeliveryHistory")
    public void anEmptyDeliveryHistory() {
        this.deliveryRepository.deleteAll(this.deliveryRepository.findAll());
    }
/////// fixme:Until we know how to have differents step defs
    private Fleet fleet;
    private ResultActions last_query;
    private Drone drone;

    @Given("A drone called {string}")
    public void aBasicDroneFleet(String droneId) {
        Random random = new Random();
        List<Drone> drones = new ArrayList<>();
        for (int i = 0; i < 15; i++) drones.add(new Drone( random.nextInt(100)));
        this.fleet = new Fleet(drones);
    }
/////// fixme:Until we know how to have differents step defs

    @Given("^A basic drone fleet$")
    public void aBasicDroneFleet() {
        this.anActiveDroneFleet();
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
        jsonNode.elements().forEachRemaining(element -> {
            batteryLevels.add(element.asDouble());
        });

        Assert.assertEquals(15, batteryLevels.size());
    }

    @And("An active drone")
    public void anActiveDroneNamed() {
        Drone drone = new Drone( new Random().nextInt(100));
        drone = droneRepository.save(drone);
        this.activeDrone = drone;
    }

    @When("Elena calls the active back to base")
    public void elenaCallsBackToBase() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + this.activeDrone.getDroneID() + "/" + DroneStatus.CALLED_HOME + "");
        this.last_query = mockMvc.perform(req).andExpect(status().isOk());
    }


    @Then("Drone active status is {string}")
    public void sStatusIs(String statusName) {
        DroneStatus status = DroneStatus.find(statusName).get();
        Iterable<Drone> drones = droneRepository.getDronesByDroneStatus(status);
        assertTrue(drones.iterator().hasNext());
        // assert drone has this status
    }
    @And("^The drone has distance to target of (\\d+)m$")
    public void theDroneHasDistanceToTargetOfM(int distance) {
        Whereabouts whereabouts = new Whereabouts();
        whereabouts.setDistanceToTarget(distance);
        this.free_drone.setWhereabouts(whereabouts);
        this.whereaboutsRepository.save(whereabouts);
        this.droneRepository.save(free_drone);
    }
    @When("^The distance goes under (\\d+)m$")
    public void theDistanceGoesUnderM(int distance) {

    }
    @Then("^The OrderService receives a notification$")
    public void theOrderServiceReceivesANotification() {

    }

    @When("^Elena asks to set the drone aside$")
    public void elenaAsksToSetAside() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + free_drone.getDroneID() + "/" + ASIDE + "");
        this.last_response = mockMvc.perform(req)
                .andExpect(status().isOk());
    }
    @Then("The drone's status is {string}")
    public void theDroneSStatusIs(String statusName) {
        free_drone = droneRepository.findById(free_drone.getDroneID()).get();
        DroneStatus status = DroneStatus.find(statusName).get();
        Assert.assertTrue(free_drone.is(status));
    }

    @When("^Elena asks to set the drone ready for service$")
    public void elenaAsksToSetReadyForService() throws Exception {
        MockHttpServletRequestBuilder req = put("/drone/set_drone_aside/" + free_drone.getDroneID() + "/" + ACTIVE + "");
        this.last_response = mockMvc.perform(req)
                .andExpect(status().isOk());
    }
}
