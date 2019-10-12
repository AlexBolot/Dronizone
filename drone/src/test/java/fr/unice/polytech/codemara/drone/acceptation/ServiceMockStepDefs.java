package fr.unice.polytech.codemara.drone.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.*;
import fr.unice.polytech.codemara.drone.entities.command.CommandType;
import fr.unice.polytech.codemara.drone.entities.command.DeliveryCommand;
import fr.unice.polytech.codemara.drone.entities.command.DroneCommand;
import fr.unice.polytech.codemara.drone.repositories.DeliveryRepository;
import fr.unice.polytech.codemara.drone.repositories.DroneRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static fr.unice.polytech.codemara.drone.entities.DroneStatus.ACTIVE;
import static fr.unice.polytech.codemara.drone.entities.DroneStatus.CALLED_HOME;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Here a regrouped all step defs relating to service mock, from initialization, to stubing, verification and tear down
 */
public class ServiceMockStepDefs {

    private IntegrationContext context = IntegrationContext.getInstance();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DroneRepository droneRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;


    @And("A mocked Order Service")
    public void aMockedOrderService() {
        int serverPort = 20000;
        System.setProperty("ORDER_SERVICE_HOST", "http://localhost:" + serverPort + "/");
        if (this.context.clientServer == null) {
            this.context.clientServer = startClientAndServer(serverPort);
            this.context.mockServer = new MockServerClient("localhost", serverPort);
        }
        this.context.mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/order/notify/cancel/.*")
                ).respond(response(""));
        this.context.mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/order/notify/delivery/.*")
                ).respond(response(""));
        ;
    }

    @Then("^The OrderService receives (\\d+) delivery notification$")
    public void theOrderServiceReceivesANotification(int notificationCount) throws InterruptedException {
        Thread.sleep(1000);
        this.context.mockServer.verify(
                request().withPath("/order/notify/delivery/"+this.context.currentDrone.getCurrentDelivery().getOrderId()).withMethod("GET"),
                VerificationTimes.exactly(notificationCount)
        );
    }


    @And("The mock server is teared down")
    public void theMockServerIsTearedDown() {
        System.out.println("teardown");
        if (this.context.clientServer != null) {
            this.context.clientServer.stop();
            this.context.clientServer = null;
        }
        this.context.mockServer = null;
    }

    @And("A Mocked External Drone Commander")
    public void aMockedExternalDroneCommander() {
        int serverPort = 20000;
        System.setProperty("EXTERNAL_DRONE_HOST", "http://localhost:" + serverPort + "/");
        if (this.context.clientServer == null) {
            serverPort = 20000;
            this.context.clientServer = startClientAndServer(serverPort);
            context.mockServer = new MockServerClient("localhost", serverPort);
        }
        context.mockServer
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
                                    if (context.kafkaTemplate != null) {
                                        DroneState droneState = new DroneState(100, new Whereabouts(0, new Location(45, 7), 100, 200), command.path("target").path("droneID").asLong(), CALLED_HOME);
                                        context.kafkaTemplate.send("drones", new ObjectMapper().writeValueAsString(droneState));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return response("");
                        }
                );
    }

    @Then("A delivery canceled notification is sent to the order service for each delivery")
    public void aDeliveryCanceledNotificationIsSentToTheOrderService() {

        for (Delivery delivery : deliveryRepository.findAll()) {
            this.context.mockServer.verify(
                    request().withPath("/order/notify/cancel/" + delivery.getOrderId()).withMethod("GET")
            );
        }
    }


    @Then("A Callback Command is Issued for all drones")
    public void aCallbackCommandIsIssuedForAllDrones() throws JsonProcessingException {
        for (Drone drone :
                droneRepository.findAll()) {
            ObjectMapper mapper = new ObjectMapper();
            drone.setDroneStatus(ACTIVE);
            String droneCallbackJson = mapper.writeValueAsString(new DroneCommand(CommandType.CALLBACK).copyWith(drone));
            this.context.mockServer.verify(
                    request()
                            .withPath("/commands")

            );
        }

    }

    @Then("A delivery command is sent to an available drone")
    public void aDeliveryCommandIsSentToAnAvailableDrone() throws JsonProcessingException {
        DeliveryCommand deliveryCommand = new DeliveryCommand();
        deliveryCommand.setDelivery(deliveryRepository.findByOrderIdAndItemId(this.context.currentDelivery.getOrderId(), this.context.currentDelivery.getItemId()));
        deliveryCommand.setTarget(this.context.currentDrone);
        this.context.mockServer.verify(
                request().withPath("/commands").withBody(new ObjectMapper().writeValueAsString(deliveryCommand))
        );
    }

}
