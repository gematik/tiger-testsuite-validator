Feature: Test OptGroup behaviour 1

  behaviours / cases to test:
  opt group has been executed with all scenarios green -> OK
  opt group has been executed with all scenarios and one failure -> NOK
  opt group has been executed with all BUT ONE scenario and all executed green -> NOK
  scenario that has two optgroups and thus all scenarios of both groups must be executed and green -> OK

  @OptGroup:Test1
  Scenario: scenario1-01
    Given this step is OK

  @OptGroup:Test1
  Scenario: scenario1-02
    Given this step is OK

  @SkipExecForTest3
  @OptGroup:Test1
  Scenario: scenario1-03
    Given this step is OK

  @OptGroup:Test2
  Scenario: scenario1-04
    Given this step is OK

  @OptGroup:Test2
  Scenario: scenario1-05
    Given this step is OK

  @Failed
  @OptGroup:Test2
  Scenario: scenario1-06
    Given this step is not OK

