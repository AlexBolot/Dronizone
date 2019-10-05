Feature: PendingDispatchListing
  Scenario: Klaus list pending order dispatch
    Given A basic order list
    When Klaus queries the pending dispatch list
    Then The client receives a 200 status code
    And The client receives the basic order list
  Scenario: Klaus set an order ready to be delivered
    Given A basic order list
    And A mocked drone server
    When Klaus sets a query ready for delivery
    Then The client receives a 200 status code
    And The mock drone server receives a post query
