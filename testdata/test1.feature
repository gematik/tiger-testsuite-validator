Feature: Test feature

  @Mandatory
  Scenario: Scenario1
    When step1
    Then step2

  @Optional
  Scenario: Scenario2
    When step3
    And step4
    Then step5

  Scenario: Scenario3
    When step3
    And step4
    Then step5

  Scenario: #####
    Given test
    # Asso

