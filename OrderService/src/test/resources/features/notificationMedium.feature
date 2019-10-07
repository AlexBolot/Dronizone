Feature: Roger want to change his notification medium
  Background:
    Given There are no customer

  Scenario: Roger wants to be notified by SMS
    Given A customer
    When The customer asks to be notified by "SMS"
    Then The client will receive "SMS" as confirmation
    And His notification medium is set to "SMS"
    And The mock server is teared down

  Scenario: Roger wants to be notified by SMS
    Given A customer
    When The customer asks to be notified by "SMS"
    Then The client will receive "SMS" as confirmation
    And His notification medium is set to "SMS"
    And The mock server is teared down