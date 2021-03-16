package com.example.calculator;

public enum Number {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    PI("3.1415926536", "π"),
    EULER("2.7182818285", "e");

    private final String value;
    private final String displayValue;

    Number(String value) {
        this.value = value;
        this.displayValue = value;
    }

    Number(String value, String displayValue) {
        this.value = value;
        this.displayValue = displayValue;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return String.valueOf(getDisplayValue());
    }
}
