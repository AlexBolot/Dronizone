Feature: DroneUpdating
  Background:
    Given A Mocked External Drone Commander

  Scenario: A drone want to send it's whereabouts
    Given An active Drone Fleet
    When Elena wants to know the battery levels of the fleet
    Then She receives the list of every drone and their battery level
    And The mock server is teared down

  Scenario: When the distance goes below the 200m thresholds the drone service pings the order service
    Given An empty fleet
    And A mocked Order Service
    And A delivering drone
    And mocked drone publishers
    And The drone has distance to target of 250m
    When The drone has distance to target of 100m
    Then The OrderService receives 1 delivery notification
    And The mock server is teared down

  Scenario: When the distance goes under 200m the notification is only sent once
    Given A delivering drone
    And mocked drone publishers
    And A mocked Order Service
    And The drone has distance to target of 250m
    When The drone has distance to target of 100m
    And The drone has distance to target of 100m
    Then The OrderService receives 1 delivery notification
    And The mock server is teared down
