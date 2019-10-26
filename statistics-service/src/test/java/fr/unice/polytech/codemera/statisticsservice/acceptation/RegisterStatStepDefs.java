package fr.unice.polytech.codemera.statisticsservice.acceptation;

import fr.unice.polytech.codemera.statisticsservice.entities.Statistics;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

public class RegisterStatStepDefs {

    @Autowired
    InfluxDB influxDB;

    @Autowired
    KafkaTemplate kafkaTemplate;

    private int entries;

    @Given("A command to be packed")
    public void aCommandToBePacked() {
        Query query = new Query("Select * from orders", "dronazone");
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<Statistics> statisticsList = resultMapper
                .toPOJO(influxDB.query(query), Statistics.class);
        entries = statisticsList.size();
        doAnswer(invocationOnMock -> {
            return null;
        }).when(influxDB).write(anyString());
    }

    @When("Klaus packs the order")
    public void klausPacksTheOrder() {
        kafkaTemplate.send("orders", "{\"order_id\":1,\"status\":\"packed\"");

    }

    @Then("a new entry is registred in the database")
    public void aNewEntryIsRegistredInTheDatabase() {
        Query query = new Query("Select * from orders", "dronazone");
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<Statistics> statisticsList = resultMapper
                .toPOJO(influxDB.query(query), Statistics.class);
        assertEquals(entries + 1, statisticsList.size());
    }

    @Given("A command to be delivered")
    public void aCommandToBeDelivered() {
    }

    @When("the order is delivered")
    public void theOrderIsDelivered() {
    }

}
