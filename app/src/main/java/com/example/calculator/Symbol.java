package com.example.calculator;

public enum Symbol {
    OPEN_BRACKET('('),
    CLOSED_BRACKET(')'),
    COMMA(','),
    DOT('.');

    private final char symbol;

    Symbol(char symbol) {
        this.symbol = symbol;
    }

    public char getValue(){
        return symbol;
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}
