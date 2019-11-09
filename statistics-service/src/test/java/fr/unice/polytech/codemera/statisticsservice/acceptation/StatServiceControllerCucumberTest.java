package fr.unice.polytech.codemera.statisticsservice.acceptation;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;

//@ActiveProfiles("test")
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", plugin = {"pretty", "json:target/cucumber-report.json"})
public class StatServiceControllerCucumberTest {
    @ClassRule
    public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, "orders");

    @BeforeClass
    public static void beforeAll() {
        System.out.println("broker in the cucumber runner " + rule.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("spring.kafka.bootstrap-servers",
                rule.getEmbeddedKafka().getBrokersAsString());
    }
}