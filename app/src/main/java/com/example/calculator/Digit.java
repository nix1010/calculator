package com.example.calculator;

public enum Digit {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9);

    private final int number;

    Digit(int number) {
        this.number = number;
    }

    public int getValue() {
        return number;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }
}
