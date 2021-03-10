package com.example.calculator;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.mariuszgromada.math.mxparser.Expression;

public class MainActivity extends AppCompatActivity {

    private Toast toast;
    private EditText inputExpression;
    private TextView resultExpression;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toast = new Toast(this);

        inputExpression = findViewById(R.id.input_expression);
        resultExpression = findViewById(R.id.result_expression);

        //TODO check different solution
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputExpression.setShowSoftInputOnFocus(false);
        }

        if (savedInstanceState != null) {
            String inputExpressionValue = savedInstanceState.getString(Constant.INPUT_EXPRESSION_VALUE);
            String resultExpressionValue = savedInstanceState.getString(Constant.RESULT_EXPRESSION_VALUE);

            if (inputExpressionValue != null) {
                inputExpression.setText(inputExpressionValue);
            }

            if (resultExpressionValue != null) {
                resultExpression.setText(resultExpressionValue);
            }
        } else {
            reset();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putCharSequence(Constant.INPUT_EXPRESSION_VALUE, inputExpression.getText());
        outState.putCharSequence(Constant.RESULT_EXPRESSION_VALUE, resultExpression.getText());

        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void onClickInsertResult(View view) {
        inputExpression.setText(resultExpression.getText());
        inputExpression.setSelection(inputExpression.getText().length());
        resultExpression.setText(Constant.EMPTY_STRING);
    }

    public void onClickInsertPositiveNegativeSign(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        int idx = inputExpression.getSelectionStart();

        insertExpression(expressionBuilder, Constant.EMPTY_STRING);

        while (idx - 1 >= 0 && (isNumeric(expressionBuilder.charAt(idx - 1)) || expressionBuilder.charAt(idx - 1) == Symbol.COMMA.getValue())) {
            --idx;
        }

        if (idx - 1 >= 0 && expressionBuilder.charAt(idx - 1) == Operation.SUBTRACTION.getDisplayValue()
                && idx - 2 >= 0 && expressionBuilder.charAt(idx - 2) == Symbol.OPEN_BRACKET.getValue()) {
            expressionBuilder.replace(idx - 2, idx, Constant.EMPTY_STRING);
        } else {
            expressionBuilder.insert(idx, Operation.SUBTRACTION.toString())
                    .insert(idx, Symbol.OPEN_BRACKET.toString());
        }

        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEdit(expressionBuilder);

        calculate();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickInsertDigit(View view) {
        CharSequence expression = inputExpression.getText();
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
        StringBuilder expressionBuilder = new StringBuilder(expression);

        insertExpression(expressionBuilder, digit);
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEdit(expressionBuilder);

        calculate();
    }

    public void onClickInsertDot(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        insertExpression(expressionBuilder, Symbol.DOT);
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEdit(expressionBuilder);

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
            case R.id.btn_power:
                operation = Operation.POWER;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }

        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        insertExpression(expressionBuilder, operation);
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEdit(expressionBuilder);

        calculate();
    }

    public void onClickDelete(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        int selectionStart = inputExpression.getSelectionStart();
        int selectionEnd = inputExpression.getSelectionEnd();

        if (selectionStart > 0 && selectionStart == selectionEnd) {
            --selectionStart;
            if (expressionBuilder.charAt(selectionStart) == Symbol.COMMA.getValue()) {
                expressionBuilder.replace(selectionStart, selectionEnd, Constant.EMPTY_STRING);
                --selectionStart;
                --selectionEnd;
            }

            expressionBuilder.replace(selectionStart, selectionEnd, Constant.EMPTY_STRING);
        } else {
            insertExpression(expressionBuilder, Constant.EMPTY_STRING);
        }

        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEdit(expressionBuilder);

        if (expressionBuilder.length() == 0) {
            reset();
        } else {
            calculate();
        }
    }

    public void onClickInsertOpenClosedParenthesis(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        insertExpression(expressionBuilder, getParenthesis());
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEdit(expressionBuilder);

        calculate();
    }

    private Symbol getParenthesis() {
        int selectionEnd = inputExpression.getSelectionEnd();
        CharSequence expression = inputExpression.getText();
        boolean closeableParenthesis = false;
        int parenthesis = 0;

        for (int i = 0; i < selectionEnd; ++i) {
            if (expression.charAt(i) == Symbol.OPEN_BRACKET.getValue()) {
                ++parenthesis;
            } else {
                if (expression.charAt(i) == Symbol.CLOSED_BRACKET.getValue()) {
                    --parenthesis;
                }
                closeableParenthesis = parenthesis > 0;
            }
        }

        return closeableParenthesis ? Symbol.CLOSED_BRACKET : Symbol.OPEN_BRACKET;
    }

    public void onClickClear(View view) {
        reset();
    }

    private void reset() {
        inputExpression.setText(Constant.EMPTY_STRING);
        resultExpression.setText(Constant.EMPTY_STRING);
    }

    private void showToast(String message) {
        toast.cancel();
        toast.setText(message);
        toast.show();
    }

    private void insertExpression(StringBuilder expressionBuilder, Object expression) {
        int selectionStart = inputExpression.getSelectionStart();
        int selectionEnd = inputExpression.getSelectionEnd();

        if (selectionEnd > selectionStart) {
            expressionBuilder.replace(selectionStart, selectionEnd, expression.toString());
        } else {
            expressionBuilder.insert(selectionStart, expression.toString());
        }
    }

    private void insertExpressionToEdit(StringBuilder expressionBuilder) {
        int selectionStart = inputExpression.getSelectionStart();
        int lengthBeforeInsert = inputExpression.length();
        int lengthAfterInsert = expressionBuilder.length();

        inputExpression.setText(expressionBuilder);

        if (inputExpression.isFocused()) {
            int moveCursor = lengthAfterInsert - lengthBeforeInsert;
            if (moveCursor + selectionStart < 0) {
                moveCursor = 0;
            }
            inputExpression.setSelection(selectionStart + moveCursor);
        } else {
            inputExpression.setSelection(lengthAfterInsert);
        }
    }

    private boolean isOperation(char expression) {
        for (Operation operation : Operation.values()) {
            if (expression == operation.getDisplayValue()) {
                return true;
            }
        }

        return false;
    }

    private boolean isNumeric(char character) {
        return Character.isDigit(character) || character == Symbol.DOT.getValue();
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
            } else if (!isNumeric(expression.charAt(idxStart))) {
                return -1;
            }

            --idxStart;
        }

        return idxStart;
    }

    private void formatExpression(StringBuilder expressionBuilder) {
        char previousCharacter;
        int numberIdx = 0;
        int operations = 0;
        boolean isDecimal = false;

        for (int i = 0; i >= 0 && i < expressionBuilder.length(); ++i) {
            char character = expressionBuilder.charAt(i);
            if (i > 0) {
                previousCharacter = expressionBuilder.charAt(i - 1);
            } else {
                previousCharacter = Constant.EMPTY_CHAR_VALUE;
            }

            if (character == Symbol.DOT.getValue()) {
                if (!isDecimal) {
                    if (!isNumeric(previousCharacter)) {
                        expressionBuilder.insert(i++, Digit.ZERO);
                    }
                    isDecimal = true;
                    numberIdx = 0;
                } else {
                    expressionBuilder.replace(i, i + 1, Constant.EMPTY_STRING);
                    --i;
                }
            } else {
                if (isNumeric(character)) {
                    if (previousCharacter == Symbol.CLOSED_BRACKET.getValue()) {
                        expressionBuilder.insert(i++, Operation.MULTIPLICATION);
                    }
                    if (!isDecimal && numberIdx == 1 && previousCharacter == Character.forDigit(Digit.ZERO.getValue(), 10)) {
                        expressionBuilder.replace(i - 1, i, Constant.EMPTY_STRING);
                        i -= 2;
                        --numberIdx;
                    } else if (isDecimal && numberIdx == Constant.MAX_DECIMAL_LENGTH) {
                        expressionBuilder.replace(i, i + 1, Constant.EMPTY_STRING);
                        --i;
                        showToast(getResources().getString(R.string.max_decimal_length, String.valueOf(Constant.MAX_DECIMAL_LENGTH)));
                    } else if (!isDecimal && numberIdx == Constant.MAX_DIGIT_LENGTH) {
                        expressionBuilder.replace(i, i + 1, Constant.EMPTY_STRING);
                        --i;
                        showToast(getResources().getString(R.string.max_number_length, String.valueOf(Constant.MAX_DIGIT_LENGTH)));
                    } else {
                        ++numberIdx;
                    }
                } else {
                    if (character != Symbol.COMMA.getValue()) {
                        numberIdx = 0;
                    }
                    isDecimal = false;

                    if (character == Symbol.OPEN_BRACKET.getValue()) {
                        if (previousCharacter == Symbol.CLOSED_BRACKET.getValue() || isNumeric(previousCharacter)) {
                            expressionBuilder.insert(i, Operation.MULTIPLICATION);
                        }
                    } else if (isOperation(character)) {
                        if (operations == Constant.MAX_OPERATIONS) {
                            expressionBuilder.replace(i, i + 1, Constant.EMPTY_STRING);
                            --i;
                            showToast(getResources().getString(R.string.max_operations, String.valueOf(Constant.MAX_OPERATIONS)));
                        } else {
                            if (isOperation(previousCharacter)) {
                                expressionBuilder.replace(i - 1, i, Constant.EMPTY_STRING);
                                i -= 2;
                                --operations;
                            } else if ((previousCharacter == Constant.EMPTY_CHAR_VALUE || previousCharacter == Symbol.OPEN_BRACKET.getValue())
                                    && character != Operation.SUBTRACTION.getDisplayValue()) {
                                expressionBuilder.replace(i, i + 1, Constant.EMPTY_STRING);
                                --i;
                            } else {
                                ++operations;
                            }
                        }
                    }
                }
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

            expressionBuilder.replace(decimalDotIdx, expressionBuilder.length(), Constant.EMPTY_STRING);
        }
    }

    private void calculate() {
        String expression = inputExpression.getText().toString()
                .replaceAll(Symbol.COMMA.toString(), Constant.EMPTY_STRING)
                .replaceAll(Operation.MULTIPLICATION.toString(), String.valueOf(Operation.MULTIPLICATION.getValue()))
                .replaceAll(Operation.DIVISION.toString(), String.valueOf(Operation.DIVISION.getValue()));

        Expression expressionCalculation = new Expression(expression);

        if (expressionCalculation.checkSyntax()) {
            StringBuilder resultBuilder = new StringBuilder(String.valueOf(expressionCalculation.calculate()));
            trimDecimalZeros(resultBuilder);
            insertCommasInNumbers(resultBuilder);

            resultExpression.setText(resultBuilder);
        }
    }
}