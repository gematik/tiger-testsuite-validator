@Mandatory
Feature: Invalid Dublette

  Background:
    Given TGR clear recorded messages

  Scenario Outline:  invalid dublette
    And TGR show <color> text "${my.string}"
    Examples: We use this data only for testing data variant display in workflow ui, there is no deeper sense in it
      | color  | inhalt |
      | red    | Dagmar |
      | blue   | Nils   |
      | green  | Tim    |
      | yellow | Sophie |
