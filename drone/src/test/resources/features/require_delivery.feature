
Feature: Requiring delivery

  Background:
    Given An empty fleet
    And An empty DeliveryHistory

  Scenario: Klaus requires a delivery with available drone
    Given A Mocked External Drone Commander
    And a free drone
    When Klaus requires a delivery
    Then A delivery command is sent to an available drone
    And The sent delivery is registered
    And The mock server is teared down

  Scenario: Klaus requires a delivery with no available drone
    Given A Mocked External Drone Commander
    Given A Mocked shipment-refused Listener
    When Klaus requires a delivery
    And A pause of 1 seconds
    Then The delivery of the shipment is refused
    And The mock server is teared down