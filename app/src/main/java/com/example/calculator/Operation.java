package com.example.calculator;

public enum Operation {
    MULTIPLICATION("*", '×'),
    DIVISION("/", '÷'),
    ADDITION("+"),
    SUBTRACTION("-"),
    POWER("^");

    private final String operation;
    private final char displayOperation;

    Operation(String operation) {
        this.operation = operation;
        this.displayOperation = operation.charAt(0);
    }

    Operation(String operation, char displayOperation) {
        this.operation = operation;
        this.displayOperation = displayOperation;
    }

    public char getDisplayValue() {
        return displayOperation;
    }

    public String getValue(){
        return operation;
    }

    @Override
    public String toString() {
        return String.valueOf(getDisplayValue());
    }
}
