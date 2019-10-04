Feature: PendingDispatchListing
  Scenario: Klaus list pending order dispatch
    Given A basic order list
    When Klaus queries the pending dispatch list
    Then The client receives a 200 status code
    And The client receives the basic order list
