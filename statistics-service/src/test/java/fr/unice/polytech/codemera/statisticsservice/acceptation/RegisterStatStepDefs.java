package fr.unice.polytech.codemera.statisticsservice.acceptation;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.influxdb.InfluxDB;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class RegisterStatStepDefs {
    @MockBean
    InfluxDB influxDB = mock(InfluxDB.class);

    private KafkaTemplate kafkaTemplate;

    private int entries;
    private ArgumentCaptor<String> valueCapture;

    @Given("A command to be packed")
    public void aCommandToBePacked() {
        valueCapture = ArgumentCaptor.forClass(String.class);
        doNothing().when(influxDB).write(valueCapture.capture());
    }

    @When("Klaus packs the order")
    public void klausPacksTheOrder() {
        kafkaTemplate.send("order-packed", "{\"order_id\":1,\"status\":\"packed\"");

    }

    @Then("a new entry is registred in the database")
    public void aNewEntryIsRegistredInTheDatabase() {

    }

    @Given("A command to be delivered")
    public void aCommandToBeDelivered() {
    }

    @When("the order is delivered")
    public void theOrderIsDelivered() {
    }

    @Given("A wired kafka template")
    public void aWiredKafkaTemplate() {
        Map<String, Object> senderProperties =
                KafkaTestUtils.senderProps(
                        System.getProperty("spring.kafka.bootstrap-servers"));

        // create a Kafka producer factory
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        senderProperties);

        // create a Kafka template
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

    }
}
