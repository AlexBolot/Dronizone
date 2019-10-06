Feature: DroneUpdating
  Background:
    Given An active Drone Fleet
    And A Mocked External Drone Commander
    And A mocked Order Service

  Scenario: A drone want to send it's whereabouts
    Given An active Drone Fleet
    When Elena wants to know the battery levels of the fleet
    Then She receives the list of every drone and their battery level
    And The mock server is teared down

  Scenario: When the distance goes below the 200m thresholds the drone service pings the order service
    Given a free drone
    And The drone has distance to target of 250m
    When The distance goes under 200m
    Then The OrderService receives a notification
    And The mock server is teared down