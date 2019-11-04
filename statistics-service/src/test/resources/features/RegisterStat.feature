Feature: Register stat

  Background:
    Given A wired kafka template

  Scenario:Register a command packed
    Given A command to be packed
    When Klaus packs the order
    Then a new entry is registred in the database

  Scenario:Registers a command delivered
    Given A command to be delivered
    When the order is delivered
    Then a new entry is registred in the database

