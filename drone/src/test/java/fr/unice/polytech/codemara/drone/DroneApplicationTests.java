package fr.unice.polytech.codemara.drone;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DroneApplicationTests {

	@Test
	public void contextLoads() {
	}

	@ClassRule
	public static EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, "drones");
	@BeforeClass
	public static void beforeAll(){
		System.out.println("broker in the cucumber runner "+ rule.getEmbeddedKafka().getBrokersAsString());
		System.setProperty("spring.kafka.bootstrap-servers",
				rule.getEmbeddedKafka().getBrokersAsString());

	}
}
