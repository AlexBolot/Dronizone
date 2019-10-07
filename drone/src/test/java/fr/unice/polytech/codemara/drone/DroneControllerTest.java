package fr.unice.polytech.codemara.drone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Location;
import fr.unice.polytech.codemara.drone.entities.Whereabouts;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DroneControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Ignore
    @Test
    public void update_battery_status() throws Exception {

        String myjson = "{\"droneID\":\"aaa2\", \"battery_level\":80, \"whereabouts\":{\"location\":{\"latitude\":10.0,\"longitude\":10.0}, \"altitude\":12, \"distanceToTarget\":250}}";
        JsonNode tree = new ObjectMapper().readTree(myjson);
        Whereabouts data = new Whereabouts();
        data.setAltitude(12);
        //data.setBatteryLevel(85);
        data.getLocation().setLatitude(12.1324);
        data.getLocation().setLongitude(4.3456);
        data.setDistanceToTarget(500);

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(data);

        MockHttpServletRequestBuilder req = post("/drone/update_battery_status")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(myjson);

        this.mockMvc.perform(req).andExpect(status().isOk());
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