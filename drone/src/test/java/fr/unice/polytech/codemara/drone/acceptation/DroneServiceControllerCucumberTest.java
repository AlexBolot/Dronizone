package fr.unice.polytech.codemara.drone.acceptation;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", plugin = {"pretty", "json:target/cucumber-report.json"})
public class DroneServiceControllerCucumberTest {
    @ClassRule
    public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, 1,
            "drone-status",
            "drone-commands",
            "drone-delivery-update",
            "order-delivered",
            "order-packed",
            "order-cancelled",
            "order-soon",
            "shipment-refused");
    @BeforeClass
    public static void beforeAll(){
        System.out.println("broker in the cucumber runner " + rule.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("spring.kafka.bootstrap-servers",
                rule.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("spring.kafka.partitions-per-topics",
                String.valueOf(rule.getEmbeddedKafka().getPartitionsPerTopic()));
    }

    @AfterClass
    public static void afterAll() {

    }
}