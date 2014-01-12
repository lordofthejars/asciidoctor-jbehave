package org.jbehave.asciidoctor.reporter.calculator.steps;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.jbehave.asciidoctor.reporter.calculator.Calculator;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;


public class AddTwoNumbersSteps {
    private Calculator calculator;

    @Given("an add calculator")
    public void givenACalculator() {
        calculator = new Calculator();
    }

    @When("I add <number1> and <number2>")
    public void whenIAddNumber1AndNumber2(@Named("number1")int number1, @Named("number2")int number2) {
        calculator.add(number1, number2);
    }

    @Then("the result should <result>")
    public void thenTheOutcomeShould(@Named("result")int result) {
        assertThat(calculator.getResult(), is(result));
    }
}
