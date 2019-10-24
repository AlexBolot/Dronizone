Feature: Register stat

  Scenario:
    Given A command to be packed
    When Klaus packs the order
    Then a new entry is registred in the database

    Given A command to be delivered
    When the order is delivered
    Then a new entry is registred in the database

