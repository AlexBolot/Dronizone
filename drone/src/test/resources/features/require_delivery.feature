
Feature: Requiring delivery
  Background:
    Given An empty fleet
    And An empty DeliveryHistory
  Scenario: Klaus requires a delivery
    Given A Mocked External Drone Commander
    And a free drone
    When Klaus requires a delivery
    Then A delivery command is sent to an available drone
    And The sent delivery is registered
    And The mock server is teared down