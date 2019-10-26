package fr.unice.polytech.codemara.drone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Location;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DroneControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @ClassRule
    public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, "drones");
    @BeforeClass
    public static void beforeAll(){
        System.out.println("broker in the cucumber runner "+ rule.getEmbeddedKafka().getBrokersAsString());
        System.setProperty("spring.kafka.bootstrap-servers",
                rule.getEmbeddedKafka().getBrokersAsString());

    }

    @Test
    public void delivery() throws JsonProcessingException {
        Delivery delivery = new Delivery();
        delivery.setOrderId(1);
        delivery.setItemId(1);
        delivery.setPickup_location(new Location(12,12));
        delivery.setTarget_location(new Location(12,12));
        System.out.println(new ObjectMapper().writeValueAsString(delivery));
    }
}