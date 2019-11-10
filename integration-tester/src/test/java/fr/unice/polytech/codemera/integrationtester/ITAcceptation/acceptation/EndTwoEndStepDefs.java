package fr.unice.polytech.codemera.integrationtester.ITAcceptation.acceptation;

import com.fasterxml.jackson.databind.JsonNode;
import gherkin.deps.com.google.gson.JsonArray;
import gherkin.deps.com.google.gson.JsonElement;
import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EndTwoEndStepDefs {

    private static final String ORDER_PORT = "8082";
    private static final String ORDER_HOST = "localhost";
    private static final String WAREHOUSE_HOST = "localhost";
    private static final String WAREHOUSE_PORT = "8081";
    private String customer_name = "Roger";
    private RestTemplate restTemplate;
    private JsonElement warehouse_orders_json;
    private long customer_id = 2;
    private ArrayList<JsonElement> passedOrders = new ArrayList<>();
    private String warehouse_base_url = "http://" + WAREHOUSE_HOST + ":" + WAREHOUSE_PORT;
    private String notification_mock_url = "http://localhost:44444";
    private String drone_service_url = "http://localhost:8083";
    private String drone_mock_url = "http://localhost:8084";
    private List<Long> currentOrderIds = new ArrayList<>();
    private HashMap<Integer, Long> deliveringDronesIds = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(EndTwoEndStepDefs.class);

    @Given("All Server Started")
    public void allServerStarted() {
        serverStarted("http://localhost:8081", "Warehouse");
        serverStarted("http://localhost:8082", "Order");
        serverStarted("http://localhost:8083", "Drone");
        serverStarted("http://localhost:8084", "DroneMock");
    }

    @When("Roger passes {int} orders")
    public void rogerPassesOrders(int arg0) throws InterruptedException {

        JsonElement first_order = orderQueryJson("Item1");
        restTemplate = new RestTemplate();
        String orderServiceUrl = "http://" + ORDER_HOST + ":" + ORDER_PORT + "/order";
        // TODO: 06/11/2019 log query
        ResponseEntity<String> order1 = restTemplate.postForEntity(orderServiceUrl, first_order.getAsJsonObject().toString(), String.class);
        JsonObject result_order_one = new JsonParser().parse(order1.getBody()).getAsJsonObject().getAsJsonObject("result");
        this.customer_id = result_order_one.getAsJsonObject("customer").get("id").getAsLong();
        this.passedOrders.add(first_order.getAsJsonObject().get("params").getAsJsonObject().get("order"));
        JsonElement second_order = orderQueryJson("Item2");
        JsonObject result_order_two = new JsonParser().parse(
                Objects.requireNonNull(restTemplate.postForEntity(orderServiceUrl,
                        second_order.getAsJsonObject().toString(), String.class).getBody()))
                .getAsJsonObject().getAsJsonObject("result");
        this.currentOrderIds.add(result_order_one.get("id").getAsLong());
        this.currentOrderIds.add(result_order_two.get("id").getAsLong());
        this.passedOrders.add(second_order.getAsJsonObject().get("params").getAsJsonObject().get("order"));
    }

    /**
     * Utility method to generate a order service  query json
     *
     * @param item_name
     * @return
     */
    private JsonElement orderQueryJson(String item_name) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse("{\"id\": \"1\",\"jsonrpc\": \"2.0\",\"method\": \"orderItem\"}");
        JsonObject requestOrder = new JsonObject();
        JsonObject requestCoord = new JsonObject();
        requestCoord.addProperty("lat", 48);
        requestCoord.addProperty("lon", 7.5);
        JsonObject requestItem = new JsonObject();
        requestItem.addProperty("name", item_name);
        JsonObject requestCustomer = new JsonObject();
        requestCustomer.addProperty("name", customer_name);
        requestCustomer.addProperty("firstName", customer_name);
        requestCustomer.addProperty("id", customer_id);
        requestOrder.add("coord", requestCoord);
        requestOrder.add("item", requestItem);
        requestOrder.add("customer", requestCustomer);
        requestOrder.addProperty("paymentInfo", "pay_in_illicit_material");
        JsonObject param = new JsonObject();
        param.add("order", requestOrder);
        jsonElement.getAsJsonObject().add("params", param);
        return jsonElement;
    }

    @And("Klaus list the orders to be packed")
    public void klausListTheOrdersToBePacked() {
        ResponseEntity<String> order_response = restTemplate.getForEntity(this.warehouse_base_url + "/warehouse/orders", String.class);
        System.out.println("order_response = " + order_response);
        this.warehouse_orders_json = new JsonParser().parse(order_response.getBody());
    }

    @Then("He sees the passed orders")
    public void heSeesThePassedOrders() throws InterruptedException {
        JsonArray json_orders = this.warehouse_orders_json.getAsJsonArray();
        JsonElement first_order = json_orders.get(json_orders.size() - 2);
        JsonElement secondOrder = json_orders.get(json_orders.size() - 1);
        assertEquals(customer_id, first_order.getAsJsonObject().get("customerId").getAsLong());
        assertEquals(customer_id, secondOrder.getAsJsonObject().get("customerId").getAsLong());
    }

    @When("Klaus notifies all the passed orders are ready")
    public void klauseNotifiesAllThePassedOrdersAreReady() {
        for (JsonElement order_json :
                this.warehouse_orders_json.getAsJsonArray()) {
            long order_id = order_json.getAsJsonObject().get("orderId").getAsLong();
            restTemplate = new RestTemplate();
            restTemplate.put(this.warehouse_base_url + "/warehouse/orders/" + order_id, String.class);
        }
    }

    @Then("{int} drones receives delivery assignement commands")
    public void dronesReceivesDeliveryAssignementCommands(int arg0) {
        restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> commands = null;
        Set<JsonNode> delivery_commands_id = null;
        List<Long> longStream = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            commands = restTemplate.getForEntity(this.drone_mock_url + "/commands/debug/commands", JsonNode.class);
            JsonNode json = commands.getBody();
            for (JsonNode node :
                    json) {
                if (node.get("command").asText().equals("DELIVERY") && this.currentOrderIds.contains(node.get("delivery").get("orderId").asLong())) {
                    this.deliveringDronesIds.put(currentOrderIds.indexOf(node.get("delivery").get("orderId").asLong()), node.get("target").get("droneID").asLong());
                }

            }
            delivery_commands_id = new HashSet<>(json.findValues("orderId"));
            longStream = delivery_commands_id.stream().map(n -> n.asLong(-1)).filter(l -> this.currentOrderIds.contains(l)).collect(Collectors.toList());
            if (longStream.size() >= 2) {
                break;
            }
            try {
                Thread.sleep(
                        1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO: 25/10/2019 Uncomment for commit
        assertTrue(longStream.size() == 2);
        System.out.println(commands);
    }

    @And("Drone for the order {int} pickup it's parcel")
    public void droneForTheOrderPickupItSParcel(int orderIndex) {
        Long droneId = this.deliveringDronesIds.get(orderIndex - 1);
        restTemplate.getForEntity(this.drone_mock_url + String.format("/commands/debug/%d/finishPickup", droneId), JsonNode.class);
    }
    @When("Drone for the order {int} approches his target")
    public void droneForTheOrderApprochesHisTarget(int drone_index) {
        long droneId = this.deliveringDronesIds.get(drone_index - 1);
        restTemplate.getForEntity(this.drone_mock_url + String.format("/commands/debug/%d/finishDelivery", droneId), JsonNode.class);
    }

    @Then("A delivery notification is sent to Roger for order {int}")
    public void aDeliveryNotificationIsSentToRogerForOrder(int order_index) {
        restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> notifications = restTemplate.getForEntity(this.notification_mock_url + "/notifications/mock/notification_history", JsonNode.class);
        List<String> items = notifications.getBody().findValues("item_name").stream().map(n -> n.toString()).collect(Collectors.toList());
        JsonElement order = this.passedOrders.get(order_index);
        String itemName = order.getAsJsonObject().get("item").getAsJsonObject().get("name").toString();
        assertTrue("Should contain a notification for item " + itemName, items.contains(itemName));

    }

    @When("Drone for the order {int} delivers")
    public void droneForTheOrderDelivers(int order_index) {
        //        fail("Should trigger the mock service for delivery approach");

    }

    @And("Roger sets his notification media to SMS")
    public void rogerSetsHisNotificationMediaToSMS() {
        {

            JsonElement media_setting_query = notificationMediaQuery("SMS");

            restTemplate = new RestTemplate();
            String orderServiceUrl = "http://" + ORDER_HOST + ":" + ORDER_PORT + "/order";
            restTemplate.postForEntity(orderServiceUrl, media_setting_query.toString(), String.class);

        }
    }

    private JsonElement notificationMediaQuery(String media) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse("{\"id\": \"1\",\"jsonrpc\": \"2.0\",\"method\": \"setPersonalPreferences\"}");
        JsonObject param = new JsonObject();
        param.addProperty("customerId", customer_id);
        param.addProperty("notificationPreference", media);
        jsonElement.getAsJsonObject().add("params", param);
        return jsonElement;
    }

    @And("Elena calls back the drone for the order {int} for battery failure")
    public void elenaCallsBackTheDroneForTheOrderForBatteryFailure(int order_index) {
        restTemplate.put(this.drone_service_url + "/drone/set_drone_aside/" + 2 + "/CALLED_HOME", String.class);
    }

    @Then("The drone for order {int} receives a called home command")
    public void theDroneForOrderReceivesACalledHomeCommand(int order_index) {
        ResponseEntity<JsonNode> commands = restTemplate.getForEntity(this.drone_mock_url + "/commands/debug/commands", JsonNode.class);
        JsonNode json = commands.getBody();

        boolean command_received = false;
        for (JsonNode node :
                json) {
            if (node.get("type").toString().equals("CALLBACK") && this.deliveringDronesIds.containsValue(node.get("target").get("droneID").asLong())) {
                command_received = true;
            }
        }
        assertTrue("A Callback command for the delivering drone should be found", command_received);
    }

    @When("Elena brings the drone for order {int} back in the active fleet")
    public void elenaBringsTheDroneForOrderBackInTheActiveFleet(int order_index) {
        restTemplate.put(this.drone_service_url + "/drone/set_drone_aside/" + 2 + "/ACTIVE", String.class);
    }

    @And("Elena issues a global callback command")
    public void elenaIssuesAGlobalCallbackCommand() {
        restTemplate.postForEntity(this.drone_service_url + "/drone/fleet/command/callback", "", String.class);

    }

    @Then("Roger receives a delivery cancel notification for order {int}")
    public void rogerReceivesADeliveryCancelNotificationForOrder(int order_id) {
        restTemplate = new RestTemplate();
        ResponseEntity<String> notifications = restTemplate.getForEntity(this.notification_mock_url + "/notifications/mock/notification_history", String.class);
        System.out.println(notifications.getBody());
    }

    @And("Elena can see position history for the drone for order {int} from before the order to now")
    public void elenaCanSeePositionHistoryForTheDroneForOrderFromBeforeTheOrderToNow(int order_index) {

    }

    public boolean serverStarted(String url, String serverName) {
        RestTemplate restTemplate = new RestTemplate();
        boolean serverStarted = false;
        int count = 0;

        while (!serverStarted) {
            try {
                restTemplate.getForEntity(url, String.class);
                serverStarted = true;
            } catch (RestClientException e) {
                System.out.println("Waiting for " + serverName);
                count++;
                if (count % 60 == 0) {
                    logger.warn("It's been %d minutes, maybe check the server status to see if it is still starting", count / 30);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }
        return count < 60;
    }


    @And("A reseted drone mock")
    public void aResetedDroneMock() {
        restTemplate = new RestTemplate();
        restTemplate.getForEntity(this.drone_mock_url + "/commands/debug/reset", String.class);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
