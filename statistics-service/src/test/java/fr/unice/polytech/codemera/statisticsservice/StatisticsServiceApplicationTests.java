package fr.unice.polytech.codemera.statisticsservice;

import org.influxdb.InfluxDB;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertTrue;

//@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsServiceApplicationTests {

    @MockBean
    InfluxDB influxDB;


    @Test
    public void contextLoads() {
        assertTrue(true);
    }

    @ClassRule
    public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, "orders");

    @BeforeClass
    public static void beforeAll() {
        System.out.println("broker in the cucumber runner " + rule.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("spring.kafka.bootstrap-servers",
                rule.getEmbeddedKafka().getBrokersAsString());

    }



}
