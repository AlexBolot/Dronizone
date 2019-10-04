package fr.unice.polytech;

import fr.unice.polytech.entities.Customer;
import fr.unice.polytech.repo.CustomerRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PersistTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepo customerRepo;

    @Test
    public void test() {
        assertTrue(true);
    }
}
