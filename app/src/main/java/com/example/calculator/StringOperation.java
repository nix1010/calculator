package com.example.calculator;

public enum StringOperation {
    SINE("sin"),
    COSINE("cos"),
    TANGENT("tan"),
    LOGARITHM("log10", "log"),
    NATURAL_LOGARITHM("ln"),
    ABSOLUTE("abs");

    private final String calculationValue;
    private final String displayValue;

    StringOperation(String calculationValue) {
        this.calculationValue = calculationValue;
        this.displayValue = calculationValue;
    }

    StringOperation(String calculationValue, String displayValue) {
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
