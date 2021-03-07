package com.example.calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    private Toast toast;
    private TextView inputExpression;
    private TextView resultExpression;
    private int brackets;
    private boolean isOpeningBracket;
    private boolean isDecimalInput;
    private static final int MAX_DIGIT_LENGTH = 15;
    private static final int MAX_DECIMAL_LENGTH = 10;
    private static final char EMPTY_CHAR_VALUE = 'E';
    private static final String EMPTY_STRING = "";

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toast = new Toast(this);
        inputExpression = findViewById(R.id.expression_input);
        resultExpression = findViewById(R.id.expression_result);
        reset();
    }

    public void onClickInsertResult(View view) {
        inputExpression.setText(resultExpression.getText());
        resultExpression.setText(EMPTY_STRING);
    }

    public void onClickInsertPositiveNegativeSign(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        StringBuilder numberBuilder = new StringBuilder();
        int idx = expressionBuilder.length();

        while (idx - 1 >= 0 && isDigit(expressionBuilder.charAt(idx - 1))) {
            --idx;
            numberBuilder.insert(0, expressionBuilder.charAt(idx));
        }

        if (idx - 1 >= 0 && expressionBuilder.charAt(idx - 1) == Operation.SUBTRACTION.getValue()
                && idx - 2 >= 0 && expressionBuilder.charAt(idx - 2) == Symbol.OPEN_BRACKET.getValue()) {
            expressionBuilder.replace(idx - 2, idx, EMPTY_STRING);
            --brackets;
        } else {
            //remove number
            expressionBuilder.replace(idx, expressionBuilder.length(), EMPTY_STRING);
            insertOpenBracket(expressionBuilder);
            expressionBuilder.append(Operation.SUBTRACTION);
            expressionBuilder.append(numberBuilder);
        }

        formatExpression(expressionBuilder);
        inputExpression.setText(expressionBuilder);

        calculate();
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
        expressionBuilder.append(digit);

        if (isDecimalInput && lastNumberLength(expressionBuilder) > MAX_DECIMAL_LENGTH) {
            showToast(getResources().getString(R.string.max_decimal_length, String.valueOf(MAX_DECIMAL_LENGTH)));
            return;
        }
        if (lastNumberLength(expressionBuilder) > MAX_DIGIT_LENGTH) {
            showToast(getResources().getString(R.string.max_number_length, String.valueOf(MAX_DIGIT_LENGTH)));
            return;
        }

        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        inputExpression.setText(expressionBuilder);

        calculate();
    }

    public void onClickInsertDot(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        expressionBuilder.append(Symbol.DOT);
        formatExpression(expressionBuilder);
        isDecimalInput = true;

        inputExpression.setText(expressionBuilder);

        calculate();
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
        expressionBuilder.append(operation);
        formatExpression(expressionBuilder);

        inputExpression.setText(expressionBuilder);
    }

    public void onClickDeleteCharacter(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        if (expressionBuilder.length() > 0) {
            char lastCharacter = getLastCharacter(expressionBuilder);
            expressionBuilder.replace(expressionBuilder.length() - 1, expressionBuilder.length(), EMPTY_STRING);

            if (lastCharacter == Symbol.OPEN_BRACKET.getValue()) {
                --brackets;
                isOpeningBracket = true;
            } else if (lastCharacter == Symbol.CLOSED_BRACKET.getValue()) {
                ++brackets;
                isOpeningBracket = false;
            } else if (lastCharacter == Symbol.DOT.getValue()) {
                isDecimalInput = false;
            }

            insertCommasInNumbers(expressionBuilder);
            inputExpression.setText(expressionBuilder);

            if (expressionBuilder.length() == 0) {
                reset();
            } else {
                calculate();
            }
        }
    }

    public void onClickInsertOpenClosedBracket(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        if (brackets > 0 && isDigit(getLastCharacter(expressionBuilder))) {
            isOpeningBracket = false;
        }

        if (isOpeningBracket) {
            insertOpenBracket(expressionBuilder);
        } else {
            insertClosedBracket(expressionBuilder);
        }

        if (brackets == 0) {
            isOpeningBracket = true;
        }

        formatExpression(expressionBuilder);
        inputExpression.setText(expressionBuilder);
    }

    public void onClickClear(View view) {
        reset();
    }

    private void reset() {
        inputExpression.setText(EMPTY_STRING);
        resultExpression.setText(EMPTY_STRING);
        brackets = 0;
        isOpeningBracket = true;
        isDecimalInput = false;
    }

    private void insertOpenBracket(StringBuilder expressionBuilder) {
        ++brackets;
        expressionBuilder.append(Symbol.OPEN_BRACKET);
    }

    private void insertClosedBracket(StringBuilder expressionBuilder) {
        --brackets;
        expressionBuilder.append(Symbol.CLOSED_BRACKET);
    }

    private void showToast(String message) {
        toast.cancel();
        toast.setText(message);
        toast.show();
    }

    private int lastNumberLength(StringBuilder expressionBuilder) {
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
        return expression.length() > 0 ? expression.charAt(expression.length() - 1) : EMPTY_CHAR_VALUE;
    }

    private void insertCommasInNumbers(StringBuilder expression) {
        StringBuilder expressionBuilder = new StringBuilder();
        int numberCount = 0;
        int decimalPointIdx = findNextDecimalPoint(expression, expression.length() - 1);
        boolean numberStarted = false;

        for (int i = expression.length() - 1; i >= 0; --i) {
            if (Character.isDigit(expression.charAt(i))) {
                if (!numberStarted) {
                    decimalPointIdx = findNextDecimalPoint(expression, i);
                    numberStarted = true;
                }
                if (decimalPointIdx == -1 && ++numberCount == 4) {
                    expressionBuilder.insert(0, Symbol.COMMA.getValue());
                    numberCount = 1;
                }
            } else if (expression.charAt(i) != Symbol.COMMA.getValue()) {
                numberCount = 0;
                numberStarted = false;
            }

            if (expression.charAt(i) != Symbol.COMMA.getValue()) {
                expressionBuilder.insert(0, expression.charAt(i));
            }
        }

        expression.replace(0, expression.length(), expressionBuilder.toString());
    }

    private int findNextDecimalPoint(StringBuilder expression, int idxStart) {
        while (idxStart >= 0) {
            if (expression.charAt(idxStart) == Symbol.DOT.getValue()) {
                return idxStart;
            } else if (!isDigit(expression.charAt(idxStart))) {
                return -1;
            }

            --idxStart;
        }

        return idxStart;
    }

    private void formatExpression(StringBuilder expressionBuilder) {
        char previousCharacter;
        int numberIdx = 0;
        boolean isDecimal = false;

        for (int i = 0; i >= 0 && i < expressionBuilder.length(); ++i) {
            char character = expressionBuilder.charAt(i);
            if (i > 0) {
                previousCharacter = expressionBuilder.charAt(i - 1);
            } else {
                previousCharacter = EMPTY_CHAR_VALUE;
            }

            if (character == Symbol.DOT.getValue()) {
                isDecimal = true;

                if (!isDigit(previousCharacter)) {
                    expressionBuilder.insert(i, Digit.ZERO);
                } else if (previousCharacter == Symbol.DOT.getValue()) {
                    expressionBuilder.replace(i, i + 1, EMPTY_STRING);
                    i -= 2;
                }
            } else {
                if (isDigit(character)) {
                    ++numberIdx;

                    if (previousCharacter == Symbol.CLOSED_BRACKET.getValue()) {
                        expressionBuilder.insert(i, Operation.MULTIPLICATION);
                    } else if (!isDecimal && numberIdx == 2 && previousCharacter == Character.forDigit(Digit.ZERO.getValue(), 10)) {
                        expressionBuilder.replace(i - 1, i, EMPTY_STRING);
                        i -= 2;
                        --numberIdx;
                    }
                } else {
                    if (character != Symbol.COMMA.getValue()) {
                        numberIdx = 0;
                    }

                    if (character == Symbol.OPEN_BRACKET.getValue()) {
                        if (previousCharacter == Symbol.CLOSED_BRACKET.getValue() || isDigit(previousCharacter)) {
                            expressionBuilder.insert(i, Operation.MULTIPLICATION);
                        }
                    } else if (isOperation(character)) {
                        if (isOperation(previousCharacter)) {
                            expressionBuilder.replace(i - 1, i, EMPTY_STRING);
                            i -= 2;
                        } else if ((previousCharacter == EMPTY_CHAR_VALUE || previousCharacter == Symbol.OPEN_BRACKET.getValue())
                                && character != Operation.SUBTRACTION.getValue() && character != Operation.ADDITION.getValue()) {
                            expressionBuilder.replace(i, i + 1, EMPTY_STRING);
                            i -= 2;
                        }
                    }
                }

                isDecimal = false;
            }
        }
    }

    private void trimDecimalZeros(StringBuilder expressionBuilder) {
        int decimalDotIdx = expressionBuilder.lastIndexOf(Symbol.DOT.toString());

        if (decimalDotIdx != -1) {
            for (int i = decimalDotIdx + 1; i < expressionBuilder.length(); ++i) {
                if (expressionBuilder.charAt(i) != Character.forDigit(Digit.ZERO.getValue(), 10)) {
                    return;
                }
            }

            expressionBuilder.replace(decimalDotIdx, expressionBuilder.length(), EMPTY_STRING);
        }
    }

    public static native double calculate(String expression);

    private void calculate() {
        double result = calculate(inputExpression.getText().toString());
        StringBuilder resultBuilder = new StringBuilder(String.valueOf(result));

        trimDecimalZeros(resultBuilder);
        insertCommasInNumbers(resultBuilder);

        resultExpression.setText(resultBuilder);
    }
}