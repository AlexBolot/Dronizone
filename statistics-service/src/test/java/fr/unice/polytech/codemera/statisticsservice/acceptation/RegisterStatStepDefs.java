package fr.unice.polytech.codemera.statisticsservice.acceptation;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemera.statisticsservice.controller.StatisticsController;
import fr.unice.polytech.codemera.statisticsservice.entities.Statistics;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class RegisterStatStepDefs {
    @MockBean
    InfluxDB influxDB = mock(InfluxDB.class);

    private KafkaTemplate kafkaTemplate;

    private StatisticsController statisticsController;

    private int entries;
    private ArgumentCaptor<String> valueCapture;

    @Given("A command to be packed")
    public void aCommandToBePacked() {
    }

    @When("Klaus packs the order")
    public void klausPacksTheOrder() {
        kafkaTemplate.send("order-packed", "{\"order_id\":1,\"status\":\"packed\"");
    }

    @Then("a new entry is registred in the database")
    public void aNewEntryIsRegistredInTheDatabase() {
        System.out.println("------------------------------------Now waiting 10s");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // verify(influxDB, times(1)).query(any(Query.class));
        verify(influxDB, times(1)).write(any(Point.class));
    }

    @Given("A command to be delivered")
    public void aCommandToBeDelivered() {
    }

    @When("the order is delivered")
    public void theOrderIsDelivered() {
        kafkaTemplate.send("order-delivered", "{\"order_id\":1,\"status\":\"delivered\"");
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

        valueCapture = ArgumentCaptor.forClass(String.class);
        doNothing().when(influxDB).write(valueCapture.capture());

    }
}