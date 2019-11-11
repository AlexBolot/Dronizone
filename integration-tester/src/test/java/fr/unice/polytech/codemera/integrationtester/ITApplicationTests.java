package fr.unice.polytech.codemera.integrationtester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ITApplicationTests {
    public boolean serverStarted(String url, String serverName) {
        RestTemplate restTemplate = new RestTemplate();
        boolean serverStarted = false;
        int count = 0;
        while (!serverStarted && count < 60) {
            try {
                restTemplate.getForEntity(url, String.class);
                serverStarted = true;
            } catch (RestClientException e) {
                System.out.println("Waiting for " + serverName);
                count++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return count < 60;
    }

    @Test
    public void orderServerBoot() {
        assertTrue(serverStarted("http://localhost:8082", "Order"));
    }

    @Test
    public void droneServerBoot() {
        assertTrue(serverStarted("http://localhost:8083", "Drone"));
    }

    @Test
    public void warehouseServerBoot() {
        assertTrue(serverStarted("http://localhost:8081", "Warehouse"));
    }
}
