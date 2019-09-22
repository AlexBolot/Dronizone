package fr.unice.polytech.codemara;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppTest {

    @Test
    public void contextLoads() {
    }

    @Test
    public void travisShouldFail() {
        fail("Failing travis");
    }
}
