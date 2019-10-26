# Created by Gregoire Peltier at 16/10/2019
Feature: Full end to end delivery and tracking of a drone order
  # Enter feature description here
  Background:
    Given All Server Started
  Scenario: 2 Orders are placed, one is delivered, the other trigger's notifications
    When Roger passes 2 orders
    And Klaus list the orders to be packed
    Then He sees the passed orders
    When Klaus notifies all the passed orders are ready
    Then 2 drones receives delivery assignement commands
    When Drone for the order 1 approches his target
    Then A delivery notification is sent to Roger for order 1
    When Drone for the order 1 delivers
    And Roger sets his notification media to SMS
    And Elena calls back the drone for the order 2 for battery failure
    Then The drone for order 2 receives a called home command
    When Elena brings the drone for order 2 back in the active fleet
    And Elena issues a global callback command
    Then Roger receives a delivery cancel notification for order 2
    And Elena can see position history for the drone for order 1 from before the order to now