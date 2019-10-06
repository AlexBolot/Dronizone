Feature: DroneCommands

  Scenario: Elena issues a drone callback
    Given An active Drone Fleet
    And A Mocked External Drone Commander
    And A mocked Order Service
    When Elena callbacks the drones
    Then A Callback Command is Issued for all drones
    And All drones states is Callback
    And The mock server is teared down