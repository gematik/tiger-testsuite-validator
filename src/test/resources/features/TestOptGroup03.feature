Feature: Test OptGroup behaviour 3

  @OptGroup:Test1
  @OptGroup:Test3
  Scenario: scenario3-01
    Given this step is OK

  @SkipExecForTest4
  @OptGroup:Test3
  Scenario: scenario3-02
    Given this step is OK
