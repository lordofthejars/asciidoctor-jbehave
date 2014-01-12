Meta:
@author Alex

Narrative:
In order to quickly find out the multiply of those two numbers
As a user
I want to use a calculator to add multiply numbers

GivenStories: org/jbehave/asciidoctor/reporter/calculator/stories/calculator_add_stories.story

Scenario:  Multiply two valid numbers

Given a calculator
When I multiply <number1> and <number2>
Then the outcome should <result>

Examples:
|number1|number2|result|
|10|10|100|
|5|5|10|
