package fr.unice.polytech.dronemock;

import fr.unice.polytech.dronemock.models.Location;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DistanceTest {

    @Test
    public void distanceTest() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(45, 8);
        assertEquals(3850640.6911018877, getDistanceFromToGeographicPoint(l1, l2, alt), 0.1); // FAUX !!
        assertEquals(78626.18767687454, distance(l1.getLatitude(), l2.getLatitude(),    // VRAI !!!
                l1.getLongitude(), l2.getLongitude(), alt, alt), 0.1);
    }

    @Test
    public void movingRight() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(45, 8);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    @Test
    public void movingLeft() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(45, 6);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    @Test
    public void movingDown() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(46, 7);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    @Test
    public void movingUp() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(44, 7);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    @Test
    public void movingUpRight() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(44, 8);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    @Test
    public void movingUpLeft() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(44, 6);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    @Test
    public void movingDownRight() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(46, 8);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    @Test
    public void movingDownLeft() {
        double alt = 10;
        Location l1 = new Location(45, 7);
        Location l2 = new Location(46, 6);
        double d1 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        moveDrone(l1, l2);
        double d2 = distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), alt, alt);
        assertTrue(d1 > d2);
    }

    public void moveDrone(Location location, Location target) {
        if (location.getLatitude() >= target.getLatitude()) {
            if (location.getLongitude() >= target.getLongitude()) {
                location.setLatitude(location.getLatitude() - 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() - 0.001 * getRandomInteger(9, 1));
            } else {
                location.setLatitude(location.getLatitude() - 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() + 0.001 * getRandomInteger(9, 1));
            }
        } else {
            if (location.getLongitude() >= target.getLongitude()) {
                location.setLatitude(location.getLatitude() + 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() - 0.001 * getRandomInteger(9, 1));
            } else {
                location.setLatitude(location.getLatitude() + 0.001 * getRandomInteger(9, 1));
                location.setLongitude(location.getLongitude() + 0.001 * getRandomInteger(9, 1));
            }
        }
    }

    /*
     * returns random integer between minimum and maximum range
     */
    public static int getRandomInteger(int maximum, int minimum){
        return ((int) (Math.random()*(maximum - minimum))) + minimum;
    }

    public double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    /**
     * https://gis.stackexchange.com/questions/277171/compute-distance-between-two-point-from-longitude-latitude-height
     * @param l1 Drone position
     * @param l2 Target position
     * @return The distance between the two
     */
    public double getDistanceFromToGeographicPoint(Location l1, Location l2, double alt) {
        double a = 6378137;
        double b = 6356752.314245;
        double tmp = Math.pow(b, 2) / Math.pow(a, 2);
        double e_2 = 1 - tmp;

        double x1 = (N(l1.getLatitude(), a, b, e_2) + alt) * Math.cos(l1.getLatitude()) * Math.cos(l1.getLongitude());
        double y1 = (N(l1.getLatitude(), a, b, e_2) + alt) * Math.cos(l1.getLatitude()) * Math.sin(l1.getLongitude());
        double z1 = (tmp * N(l1.getLatitude(), a, b, e_2) + alt) * Math.sin(l1.getLongitude());

        double x2 = (N(l2.getLatitude(), a, b, e_2) + alt) * Math.cos(l2.getLatitude()) * Math.cos(l2.getLongitude());
        double y2 = (N(l2.getLatitude(), a, b, e_2) + alt) * Math.cos(l2.getLatitude()) * Math.sin(l2.getLongitude());
        double z2 = (tmp * N(l2.getLatitude(), a, b, e_2) + alt) * Math.sin(l2.getLongitude());

        double distance = Math.sqrt(
                Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2)
        );

        return distance;
    }

    private double N(double phi, double a, double b, double e_2) {
        return a / Math.sqrt(1 - e_2 * Math.pow(Math.sin(phi), 2));
    }
}
