package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import fr.unice.polytech.codemara.drone.entities.Location;
import fr.unice.polytech.codemara.drone.entities.command.CommandType;
import fr.unice.polytech.codemara.drone.entities.command.DeliveryCommand;
import fr.unice.polytech.codemara.drone.entities.command.DroneCommand;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.After;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.ACTIVE;
import static fr.unice.polytech.codemara.drone.entities.DroneStatus.CALLED_HOME;
import static org.junit.Assert.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Given("An active Drone Fleet")
    public void anActiveDroneFleet() {
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
        drone.setDroneStatus(ACTIVE);
        drone.currentDelivery = null;
        drone = droneRepository.save(drone);
        this.free_drone = drone;
    }

    @And("A Mocked External Drone Commander")
    public void aMockedExternalDroneCommander() {
        int serverPort = 20000;
        System.setProperty("EXTERNAL_DRONE_HOST", "http://localhost:" + serverPort + "/");
        this.clientServer = startClientAndServer(serverPort);
        mockServer = new MockServerClient("localhost", serverPort);
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
                                mockMvc.perform(put("/drone/set_drone_aside/" + command.path("target").path("droneID") + "/" + CALLED_HOME)).andExpect(status().isOk());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return response("");
                        }
                );
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
                            .withBody(droneCallbackJson),
                    VerificationTimes.once()
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
        test_delivery.setPickup_location(new Location(10,10));
        test_delivery.setTarget_location(new Location(11,11));
        String test_delivery_json = new ObjectMapper().writeValueAsString(test_delivery);
        this.last_delivery = test_delivery;
        MockHttpServletRequestBuilder put = put("/drone/request_delivery");
        put = put.contentType("application/json");
        mockMvc.perform(put.content(test_delivery_json)).andExpect(status().isOk());
    }

    @Then("A delivery command is sent to an available drone")
    public void aDeliveryCommandIsSentToAnAvailableDrone() throws JsonProcessingException {
        DeliveryCommand deliveryCommand = new DeliveryCommand();
        deliveryCommand.setDelivery(deliveryRepository.findByOrderIdAndItemId(this.last_delivery.getOrderId(),this.last_delivery.getItemId()));
        deliveryCommand.setTarget(this.free_drone);
        this.mockServer.verify(
                request().withPath("/commands").withBody(new ObjectMapper().writeValueAsString(deliveryCommand))
        );
    }

    @And("The sent delivery is registered")
    public void theSentDeliveryIsRegistered() {
        assertNotNull(deliveryRepository.findByOrderIdAndItemId(this.last_delivery.getOrderId(),this.last_delivery.getItemId()));
    }


    @Then("A delivery canceled notification is sent to the order service")
    public void aDeliveryCanceledNotificationIsSentToTheOrderService() {
    }

    @And("deliveries in completion")
    public void deliveriesInCompletion() {

    }



    @And("The mock server is teared down")
    public void theMockServerIsTearedDown() {
        System.out.println("teardown");
        if (this.clientServer!=null){
            this.clientServer.stop();
        }
    }

    @Given("An empty fleet")
    public void anEmptyFleet() {
        this.droneRepository.deleteAll(this.droneRepository.findAll());
    }
}
