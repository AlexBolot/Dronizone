Feature: DroneCommands

  Scenario: Elena issues a drone callback
    Given An active Drone Fleet
    And A Mocked External Drone Commander
    And A mocked Order Service
    And mocked drone publishers
    And mocked drone listener
    When Elena callbacks the drones
    Then A Callback Command is Issued for all drones
    And The mock server is teared down