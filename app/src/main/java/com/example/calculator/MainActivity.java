package com.example.calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView inputExpression;
    private TextView resultExpression;
    private int brackets;
    private boolean openingBracket;
    private static final int MAX_DIGIT_LENGTH = 15;
    private Toast toast;

    public MainActivity() {
        brackets = 0;
        openingBracket = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toast = new Toast(this);
        inputExpression = findViewById(R.id.expression_input);
        resultExpression = findViewById(R.id.expression_result);
    }

    public void onClickInsertResult(View view) {
        insertResult();
    }

    public void onClickInsertPositiveNegativeSign(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        insertPositiveNegativeSign(expressionBuilder);

        inputExpression.setText(expressionBuilder);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickInsertDigit(View view) {
        Digit digit;

        switch (view.getId()) {
            case R.id.btn_number_zero:
                digit = Digit.ZERO;
                break;
            case R.id.btn_number_one:
                digit = Digit.ONE;
                break;
            case R.id.btn_number_two:
                digit = Digit.TWO;
                break;
            case R.id.btn_number_three:
                digit = Digit.THREE;
                break;
            case R.id.btn_number_four:
                digit = Digit.FOUR;
                break;
            case R.id.btn_number_five:
                digit = Digit.FIVE;
                break;
            case R.id.btn_number_six:
                digit = Digit.SIX;
                break;
            case R.id.btn_number_seven:
                digit = Digit.SEVEN;
                break;
            case R.id.btn_number_eight:
                digit = Digit.EIGHT;
                break;
            case R.id.btn_number_nine:
                digit = Digit.NINE;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        insertDigit(expressionBuilder, digit);
        inputExpression.setText(insertCommasInNumbers(expressionBuilder.toString()));
    }

    public void onClickInsertDot(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        insertDot(expressionBuilder);

        inputExpression.setText(expressionBuilder);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickInsertOperation(View view) {
        Operation operation;

        switch (view.getId()) {
            case R.id.btn_addition:
                operation = Operation.ADDITION;
                break;
            case R.id.btn_subtraction:
                operation = Operation.SUBTRACTION;
                break;
            case R.id.btn_multiplication:
                operation = Operation.MULTIPLICATION;
                break;
            case R.id.btn_division:
                operation = Operation.DIVISION;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }

        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        insertOperation(expressionBuilder, operation);
        inputExpression.setText(expressionBuilder);
    }

    public void onClickDeleteCharacter(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        deleteCharacter(expressionBuilder);

        inputExpression.setText(insertCommasInNumbers(expressionBuilder.toString()));
    }

    public void onClickInsertOpenClosedBracket(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        insertOpenClosedBracket(expressionBuilder);

        inputExpression.setText(expressionBuilder);
    }

    public void onClickClear(View view) {
        clear();
    }

    private void insertDigit(StringBuilder expressionBuilder, Digit digit) {
        if (expressionBuilder.length() > 0 && getLastCharacter(expressionBuilder) == Symbol.CLOSED_BRACKET.getValue()) {
            insertOperation(expressionBuilder, Operation.MULTIPLICATION);
        }

        if (lastDigitLength(expressionBuilder) > MAX_DIGIT_LENGTH) {
            showToast("Maximum number of digits is: " + MAX_DIGIT_LENGTH);
        } else {
            if ((expressionBuilder.length() == 1 && Character.getNumericValue(expressionBuilder.charAt(0)) != Digit.ZERO.getValue())
                    || expressionBuilder.length() != 1) {
                expressionBuilder.append(digit);
            }
        }
    }


    public void insertOperation(StringBuilder expressionBuilder, Operation operation) {
        if (expressionBuilder.length() > 0 && isOperation(getLastCharacter(expressionBuilder))) {
            expressionBuilder.replace(expressionBuilder.length() - 1, expressionBuilder.length(), operation.toString());
        } else if (operation == Operation.SUBTRACTION ||
                expressionBuilder.length() > 0) {
            expressionBuilder.append(operation);
        }
    }

    public void insertPositiveNegativeSign(StringBuilder expressionBuilder) {
        //TODO check getSelectionStart behaviour
        StringBuilder numberBuilder = new StringBuilder();
        int idx = expressionBuilder.length();

        while (idx - 1 >= 0 && isDigit(expressionBuilder.charAt(idx - 1))) {
            --idx;
            numberBuilder.insert(0, expressionBuilder.charAt(idx));
        }

        if (idx - 1 >= 0 && expressionBuilder.charAt(idx - 1) == Operation.SUBTRACTION.getValue()
                && idx - 2 >= 0 && expressionBuilder.charAt(idx - 2) == Symbol.OPEN_BRACKET.getValue()) {
            expressionBuilder.replace(idx - 2, idx, "");
        } else {
            //remove number
            expressionBuilder.replace(idx, expressionBuilder.length(), "");
            insertOpenBracket(expressionBuilder);
            insertOperation(expressionBuilder, Operation.SUBTRACTION);
            expressionBuilder.append(numberBuilder);
        }
    }

    public void deleteCharacter(StringBuilder expressionBuilder) {
        if (expressionBuilder.length() > 0) {
            if (expressionBuilder.charAt(expressionBuilder.length() - 1) == Symbol.OPEN_BRACKET.getValue()) {
                --brackets;
            } else if (expressionBuilder.charAt(expressionBuilder.length() - 1) == Symbol.CLOSED_BRACKET.getValue()) {
                ++brackets;
            }

            openingBracket = brackets == 0;

            expressionBuilder.replace(expressionBuilder.length() - 1, expressionBuilder.length(), "");
        }
    }

    public void clear() {
        inputExpression.setText("");
        resultExpression.setText("");
        brackets = 0;
        openingBracket = true;
    }

    public void insertOpenClosedBracket(StringBuilder expressionBuilder) {
        if (expressionBuilder.length() > 0) {
            char lastCharacter = getLastCharacter(expressionBuilder);

            if (brackets > 0 && isDigit(lastCharacter)) {
                openingBracket = false;
            } else if (lastCharacter != Symbol.OPEN_BRACKET.getValue()) {
                insertOperation(expressionBuilder, Operation.MULTIPLICATION);
            }
        }

        if (openingBracket) {
            insertOpenBracket(expressionBuilder);
        } else if (brackets > 0) {
            insertClosedBracket(expressionBuilder);
        }

        if (brackets == 0) {
            openingBracket = true;
        }
    }

    private void insertOpenBracket(StringBuilder expressionBuilder) {
        ++brackets;
        expressionBuilder.append(Symbol.OPEN_BRACKET);
    }

    private void insertClosedBracket(StringBuilder expressionBuilder) {
        --brackets;
        expressionBuilder.append(Symbol.CLOSED_BRACKET);
    }

    public void insertDot(StringBuilder expressionBuilder) {
        int idx = expressionBuilder.length() - 1;
        boolean dotEntered = false;

        while (idx >= 0 && isDigit(expressionBuilder.charAt(idx))) {
            if (expressionBuilder.charAt(idx) == Symbol.DOT.getValue()) {
                dotEntered = true;
                break;
            }
            --idx;
        }

        if (!dotEntered) {
            insertDigit(expressionBuilder, Digit.ZERO);
        }

        expressionBuilder.append(Symbol.DOT);
    }

    public void insertResult() {
        inputExpression.setText(resultExpression.getText());
        resultExpression.setText("");
    }

    private void showToast(String message) {
        toast.cancel();
        toast.setText(message);
        toast.show();
    }

    private int lastDigitLength(StringBuilder expressionBuilder) {
        int idx = expressionBuilder.length() - 1;
        int digitLength = 0;

        while (idx >= 0 &&
                (Character.isDigit(expressionBuilder.charAt(idx))
                        || expressionBuilder.charAt(idx) == Symbol.COMMA.getValue())) {
            if (expressionBuilder.charAt(idx) != Symbol.COMMA.getValue()) {
                ++digitLength;
            }
            --idx;
        }

        return digitLength;
    }

    private boolean isOperation(char expression) {
        for (Operation operation : Operation.values()) {
            if (expression == operation.getValue()) {
                return true;
            }
        }

        return false;
    }

    private boolean isDigit(char lastCharacter) {
        return Character.isDigit(lastCharacter) || lastCharacter == Symbol.DOT.getValue();
    }

    private char getLastCharacter(CharSequence expression) {
        return expression.charAt(expression.length() - 1);
    }

    private CharSequence insertCommasInNumbers(String expression) {
        StringBuilder expressionBuilder = new StringBuilder();
        int numberCount = 0;

        expression = expression.replaceAll(Symbol.COMMA.toString(), "");

        for (int i = expression.length() - 1; i >= 0; --i) {
            if (Character.isDigit(expression.charAt(i))) {
                if (++numberCount == 4) {
                    expressionBuilder.insert(0, Symbol.COMMA.getValue());
                    numberCount = 1;
                }
            } else {
                numberCount = 0;
            }

            expressionBuilder.insert(0, expression.charAt(i));
        }

        return expressionBuilder.toString();
    }

    private void calculate() {

    }
}