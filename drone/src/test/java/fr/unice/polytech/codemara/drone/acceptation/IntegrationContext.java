package fr.unice.polytech.codemara.drone.acceptation;

import fr.unice.polytech.codemara.drone.entities.Delivery;
import fr.unice.polytech.codemara.drone.entities.Drone;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.kafka.core.KafkaTemplate;
import sun.security.ssl.krb5.KerberosClientKeyExchangeImpl;

import java.util.List;

/**
 * This should serve as singleton context for acceptation tests
 * ⚠ this is a sensible class, modification should be thoughtfull
 * trust yourself to do the right choices,
 * but read that your usage do not overlap with previously implemented one that you could reuse
 */
class IntegrationContext {
    private static IntegrationContext instance;
    /**
     * Mockclient and server
     */
    public MockServerClient mockServer;
    public ClientAndServer clientServer;
    /**
     * To send kafka message, carefull, drone status step defs should be in the matching stepdef file
     */
    KafkaTemplate<String, String> kafkaTemplate;

    /**
     * The current delivery, for one drone one delivery tests
     */
     Delivery currentDelivery;

    /**
     * For one drone operation, freely assign to this drone
     * For multiple drone do prefer assigning the  droneList attribute
     */
    Drone currentDrone;
    /**
     * Drone list for multi drone tests
     * for one drone test use current drone
     */
    List<Drone> currentDroneList;




    static IntegrationContext getInstance(){
        if(instance == null){
            instance = new IntegrationContext();
        }
        return instance;
    }


}
