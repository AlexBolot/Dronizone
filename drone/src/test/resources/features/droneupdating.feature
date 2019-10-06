Feature: DroneUpdating
  Scenario: A drone want to send it's whereabouts
    Given A drone called "drone1"
    When Elena wants to know the battery levels of the fleet
    Then She receives the list of every drone and their battery level
