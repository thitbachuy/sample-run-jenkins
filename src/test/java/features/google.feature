Feature: [Web] Google
  Background:
    Given the user can open the link "https://www.google.com/"

  @Google @Pass @CloseBrowser
  Scenario: [GG] Search google correct 1
    When the user enters "abc" into "correct search input" input

  @Google @Failed
  Scenario: [GG] Search google incorrect 2
    When the user enters "abc" into "correct" input
