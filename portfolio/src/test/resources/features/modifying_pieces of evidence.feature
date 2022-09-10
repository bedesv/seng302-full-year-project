Feature: U13 Modifying Pieces of Evidence

  Scenario: AC3 - When skill modified and evidence saved skill is changed.
    Given I have created evidence with skills
    When I modify a skill
    Then The skill is changed within the project