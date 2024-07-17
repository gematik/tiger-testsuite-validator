Feature: Invalid Provide a test context to test suites to maintain data between steps

  Scenario Outline: Invalid Test <color> with <text>
    Given TGR show <color> banner "<text>"
    And TGR clear recorded messages
    Assssssoooooo INVALID
    Then TGR clear recorded messages

    @aha
    Examples:
    # Test comment
      | color | text |
      | green | aha  |
    # Test comment
      | red   | bhb  |

    @Notused
    Examples:
      | color | text     |
      | red   | DONT USE |

    # Test comment
    # Test comment
    @can
    Examples:
      | color | text |
      | green | can  |
    # Test comment
      | red   | cbn  |
      | black | ccn  |
