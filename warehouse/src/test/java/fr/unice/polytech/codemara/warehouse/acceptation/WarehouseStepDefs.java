package fr.unice.polytech.codemara.warehouse.acceptation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.warehouse.entities.Location;
import fr.unice.polytech.codemara.warehouse.entities.Parcel;
import fr.unice.polytech.codemara.warehouse.entities.ParcelStatus;
import fr.unice.polytech.codemara.warehouse.entities.dto.CustomerOrder;
import fr.unice.polytech.codemara.warehouse.entities.dto.PackedOrder;
import fr.unice.polytech.codemara.warehouse.repositories.ParcelRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import junit.framework.TestCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class WarehouseStepDefs {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseStepDefs.class);

    private List<Parcel> parcels;

    private MvcResult mvcResult;
    
    private IntegrationContext context = IntegrationContext.getInstance();

    private BlockingQueue<ConsumerRecord<String, String>> records;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private MockMvc mockMvc;

    @Given("^A basic order list$")
    public void aBasicOrderList() {
        logger.info("######################## Soit une liste de commandes simples ########################");
        parcels = Arrays.asList(
                new Parcel(),
                new Parcel(),
                new Parcel()
        );
        for (int i = 0; i < parcels.size(); i++) {
            parcels.get(i).setItemId(i);
            parcels.get(i).setCustomerId(i);
            parcels.get(i).setDeliveryLocation(new Location(i, i));
            parcels.get(i).setStatus(ParcelStatus.PENDING);
            parcelRepository.save(parcels.get(i));
        }

    }

    @When("^Klaus queries the pending dispatch list$")
    public void klausQueriesThePendingDispathList() throws Exception {
        logger.info("######################## Quand Klauss demande les listes des commandes a preparer ########################");
        this.mvcResult = mockMvc.perform(get("/warehouse/parcel"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();
    }

    @Then("Klaus receive the list of pending order")
    public void klausReceiveTheListOfPendingOrder() throws IOException {
        List parcels = new ObjectMapper().readValue(this.mvcResult.getResponse().getContentAsString(), List.class);
        assertEquals(3, parcels.size());
    }

    @When("Klaus sets a query ready for delivery")
    public void klausSetsAQueryReadyForDelivery() throws Exception {
        logger.info("######################## Quand Klaus definit une commande comme prete a la livraison ########################");

        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(System.getProperty("spring.kafka.bootstrap-servers"),
                        "test", "false");
        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerProperties);
        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties("order-packed");
        if (this.context.container != null) {
            this.context.container.stop();
        }
        // create a Kafka MessageListenerContainer
        this.context.container = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);
        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>();
        // setup a Kafka message listener
        this.context.container
                .setupMessageListener((MessageListener<String, String>) record -> records.add(record));
        // start the container and underlying message listener
        this.context.container.start();
        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(this.context.container, 1);


        this.mvcResult = mockMvc.perform(put("/warehouse/parcel/1")).andReturn();
    }

    @Then("The event to request a drone is send")
    public void theEventToRequestADroneIsSend() throws InterruptedException, IOException {
        PackedOrder packedOrder = new ObjectMapper().readValue(this.mvcResult.getResponse().getContentAsString(), PackedOrder.class);
        List<ConsumerRecord<String, String>> received = new ArrayList<>();

        ConsumerRecord<String, String> poll = records.poll(1, TimeUnit.SECONDS);
        if (poll != null)
            received.add(poll);

        records.drainTo(received);
        List<String> actual = received.stream().map(ConsumerRecord::value).collect(Collectors.toList());
        assertEquals(1, actual.size());
        assertEquals(packedOrder, new ObjectMapper().readValue(actual.get(0), PackedOrder.class));
    }

    @When("A new order has been created")
    public void aNewOrderHasBeenCreated() throws Exception {
        // set up the Kafka producer properties
        Map<String, Object> senderProperties =
                KafkaTestUtils.senderProps(
                        System.getProperty("spring.kafka.bootstrap-servers"));

        // create a Kafka producer factory
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        senderProperties);

        // create a Kafka template
        context.kafkaTemplate = new KafkaTemplate<>(producerFactory);

        this.mvcResult = this.mockMvc.perform(get("/warehouse/parcel"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();

        CustomerOrder customerOrder = new CustomerOrder(88, 69, 420, new Location(10, 10), System.currentTimeMillis());
        context.kafkaTemplate.send("order-create", new ObjectMapper().writeValueAsString(customerOrder));
    }

    @Then("The orders list is bigger")
    @Ignore
    public void theOrdersListIsBigger() throws Exception {
        Thread.sleep(1000);
        int oldSize = new ObjectMapper().readValue(this.mvcResult.getResponse().getContentAsString(), List.class).size();

        this.mvcResult = this.mockMvc.perform(get("/warehouse/parcel"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();

        int newSize = new ObjectMapper().readValue(this.mvcResult.getResponse().getContentAsString(), List.class).size();

        assertEquals(oldSize + 1, newSize);
    }
}
