Feature: Notify Clients when their deliveries will arrived in 10 minutes
  Scenario: Roger already order an Item
    Given A drone with a client delivery
    When The drone is near his delivery location
    Then The drone send a notification to Order service
    And The client receives the notification that their delivery is close by