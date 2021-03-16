package com.example.calculator;

public enum CharacterOperation {
    MULTIPLICATION("*", "×"),
    DIVISION("/", "÷"),
    ADDITION("+"),
    SUBTRACTION("-"),
    POWER("^"),
    FACTORIAL("!"),
    SQUARE_ROOT("sqrt", "√");

    private final String calculationValue;
    private final String displayValue;

    CharacterOperation(String calculationValue) {
        this.calculationValue = calculationValue;
        this.displayValue = calculationValue;
    }

    CharacterOperation(String calculationValue, String displayValue) {
        this.calculationValue = calculationValue;
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getValue(){
        return calculationValue;
    }

    @Override
    public String toString() {
        return String.valueOf(getDisplayValue());
    }
}
