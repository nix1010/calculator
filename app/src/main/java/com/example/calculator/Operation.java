package com.example.calculator;

public enum Operation {
    MULTIPLICATION('*'),
    DIVISION('/'),
    ADDITION('+'),
    SUBTRACTION('-');

    private final char operation;

    Operation(char operation){
        this.operation = operation;
    }

    public char getValue(){
        return operation;
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}
