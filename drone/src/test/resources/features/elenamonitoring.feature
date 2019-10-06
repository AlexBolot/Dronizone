Feature: ElenaMonitoring
  Scenario: Elena wants to know battery levels
    Given A basic drone fleet
    When Elena wants to know the battery levels of the fleet
    Then She receives the list of every drone and their battery level

  Scenario: Elena can command a drone back to base
    Given A basic drone fleet
    And An active drone named "drone1"
    When Elena calls "drone1" back to base
    Then "drone1"'s status is "CALLED_HOME"