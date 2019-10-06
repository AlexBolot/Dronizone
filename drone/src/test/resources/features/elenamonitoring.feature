Feature: ElenaMonitoring
  Background:
    Given An empty fleet

  Scenario: Elena wants to know battery levels
    Given An active Drone Fleet
    And a free drone
    When Elena wants to know the battery levels of the fleet
    Then She receives the list of every drone and their battery level

  Scenario: Elena can command a drone back to base
    Given An active Drone Fleet
    And a free drone
    When Elena calls the drone back to base
    Then The drone's status is "CALLED_HOME"

  Scenario: Elena can notify that a drone is set aside
    Given An active Drone Fleet
    And a free drone
    When Elena asks to set the drone aside
    Then The drone's status is "ASIDE"

  Scenario: Elena can notify that sidelined drone is ready for service
    Given An active Drone Fleet
    And A sidelined drone
    When Elena asks to set the drone ready for service
    Then The drone's status is "ACTIVE"