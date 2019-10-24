package fr.unice.polytech.codemera.statisticsservice.acceptation;

import io.cucumber.java.Before;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
public class SpringCucumberContextConfiguration {
    @Before
    public void setup_cucumber_spring_context() {
        // Dummy method so cucumber will recognize this class as glue
        // and use its context configuration.
    }
}
