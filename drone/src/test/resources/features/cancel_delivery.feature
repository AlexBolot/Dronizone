Feature: DeliveryCancelled

  Scenario: CancelingNotifiesOrder
    Given An active Drone Fleet
    And A Mocked External Drone Commander
    And A mocked Order Service
    And deliveries in completion
    When Elena callbacks the drones
    Then A delivery canceled notification is sent to the order service
    And The mock server is teared down