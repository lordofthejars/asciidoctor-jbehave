package org.jbehave.asciidoctor.reporter.calculator;


public class Calculator {
    private int result;

    public void add(int number1, int number2) {
        result = number1 + number2;
    }

    public void mult(int number1, int number2) {
    	result = number1 * number2;
    }
    
    public int getResult() {
        return result;
    }
}
