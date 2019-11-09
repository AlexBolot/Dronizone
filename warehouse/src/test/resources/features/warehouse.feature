Feature: PendingDispatchListing

  Scenario: Klaus list pending order dispatch
    Given A basic order list
    When Klaus queries the pending dispatch list
    Then Klaus receive the list of pending order

  Scenario: Klaus set an order ready to be delivered
    Given A basic order list
    When Klaus just finish packing one item
    And Klaus finish packing another item
    Then Klaus decide to send the two item inside one shipment

  Scenario: A new order is created
    Given A basic order list
    When A new order has been created
    Then The orders list is bigger
