Feature: OrderingItem
  Scenario: Roger order an item of his choice
    Given An Item and the client information
    When The client will order this Item
    Then The client will receive the order as confirmation
    And The Warehouse service will receive the order