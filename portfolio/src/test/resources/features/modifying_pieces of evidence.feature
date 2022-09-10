Feature: U13 Modifying Pieces of Evidence

  Scenario: AC3 - When skill modified and evidence saved skill is changed.
    Given I have created evidence with skills
    When I modify a skill
    Then The skill is changed within the project

  Scenario: AC4 - When skill removed and evidence saved skills are updated.
    Given I have created evidence with skills
    When I remove a skill
    Then The skill is removed within the project

  Scenario: AC5 - When category removed and evidence saved categories are updated.
    Given I have created evidence with categories
    When I remove a category
    Then The category is removed within the project

  Scenario: AC5 - When category added and evidence saved categories are updated.
    Given I have created evidence with categories
    When I add a category
    Then The category is added within the project