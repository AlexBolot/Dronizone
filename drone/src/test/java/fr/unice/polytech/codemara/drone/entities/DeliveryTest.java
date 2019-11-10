package fr.unice.polytech.codemara.drone.entities;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeliveryTest {

    @Test
    public void mustNotify() {
        // Not notified and not picked_up
        Delivery delivery1  = new Delivery();
        delivery1.setNotified(false);
        delivery1.setPickedUp(false);

        // Drone is only near pickup location !
        assertFalse(delivery1.mustNotify());

        // --------------- //

        // Not notified and picked_up
        Delivery delivery2  = new Delivery();
        delivery2.setNotified(false);
        delivery2.setPickedUp(true);

        // Drone is near Roger !
        assertTrue(delivery2.mustNotify());

        // --------------- //

        // Picked_up but already notified
        Delivery delivery3  = new Delivery();
        delivery3.setNotified(true);
        delivery3.setPickedUp(true);

        // We already notifed Roger
        assertFalse(delivery3.mustNotify());
    }
}