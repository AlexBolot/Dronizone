Feature: Notify Clients when their deliveries update
  Scenario: Roger already order an Item
    Given A drone with a client delivery
    When The drone is near his delivery location
    Then The drone send a notification to Order service
    And The client receives the notification that their delivery is close by
    And The mock server is teared down

  Scenario: Roger's delivery is cancel
    Given Bad weather forecast
    When Drone is canceled by fleet manager
    Then A notification is send to client
    And The mock server is teared down