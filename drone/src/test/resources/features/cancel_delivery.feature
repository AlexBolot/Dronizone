Feature: DeliveryCancelled

  Background:
    Given An empty DeliveryHistory
  Scenario: CancelingNotifiesOrder
    Given An active Drone Fleet
    And A Mocked External Drone Commander
    And A mocked Order Service
    And deliveries in completion
    When Elena callbacks the drones
    Then A delivery canceled notification is sent to the order service for each delivery
    And The mock server is teared down