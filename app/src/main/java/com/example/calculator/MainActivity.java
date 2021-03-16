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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    private View decorView;
    private Toast toast;
    private EditText inputExpression;
    private TextView resultExpression;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener((visibility) -> {
            if (visibility == 0) {
                decorView.setSystemUiVisibility(hideSystemNavigation());
            }
        });

        toast = new Toast(this);

        inputExpression = findViewById(R.id.input_expression);
        resultExpression = findViewById(R.id.result_expression);

        //TODO check for different solution
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            decorView.setSystemUiVisibility(hideSystemNavigation());
        }
    }

    private int hideSystemNavigation() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY : 0)
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
    }

    public void onClickInsertResult(View view) {
        if (resultExpression.getText().length() > 0) {
            inputExpression.setText(resultExpression.getText());
            inputExpression.setSelection(inputExpression.getText().length());
            resultExpression.setText(Constant.EMPTY_STRING);
        }
    }

    public void onClickInsertPositiveNegativeSign(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        int idx = inputExpression.getSelectionStart();

        insertExpression(expressionBuilder, Constant.EMPTY_STRING);

        while (idx - 1 >= 0 && (isNumber(expressionBuilder.charAt(idx - 1)) || expressionBuilder.charAt(idx - 1) == Symbol.COMMA.getValue())) {
            --idx;
        }

        if (idx - 1 >= 0 && isCharacterOperation(expressionBuilder.charAt(idx - 1), CharacterOperation.SUBTRACTION)
                && idx - 2 >= 0 && expressionBuilder.charAt(idx - 2) == Symbol.OPEN_BRACKET.getValue()) {
            expressionBuilder.replace(idx - 2, idx, Constant.EMPTY_STRING);
        } else {
            expressionBuilder.insert(idx, CharacterOperation.SUBTRACTION)
                    .insert(idx, Symbol.OPEN_BRACKET);
        }

        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEditText(expressionBuilder);

        calculate();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickInsertNumber(View view) {
        Number number;

        switch (view.getId()) {
            case R.id.btn_number_zero:
                number = Number.ZERO;
                break;
            case R.id.btn_number_one:
                number = Number.ONE;
                break;
            case R.id.btn_number_two:
                number = Number.TWO;
                break;
            case R.id.btn_number_three:
                number = Number.THREE;
                break;
            case R.id.btn_number_four:
                number = Number.FOUR;
                break;
            case R.id.btn_number_five:
                number = Number.FIVE;
                break;
            case R.id.btn_number_six:
                number = Number.SIX;
                break;
            case R.id.btn_number_seven:
                number = Number.SEVEN;
                break;
            case R.id.btn_number_eight:
                number = Number.EIGHT;
                break;
            case R.id.btn_number_nine:
                number = Number.NINE;
                break;
            case R.id.btn_pi:
                number = Number.PI;
                break;
            case R.id.btn_euler_number:
                number = Number.EULER;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        insertExpression(expressionBuilder, number);
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEditText(expressionBuilder);

        calculate();
    }

    public void onClickInsertDot(View view) {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        insertExpression(expressionBuilder, Symbol.DOT);
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEditText(expressionBuilder);

        calculate();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickInsertOperation(View view) {
        CharacterOperation operation;

        switch (view.getId()) {
            case R.id.btn_addition:
                operation = CharacterOperation.ADDITION;
                break;
            case R.id.btn_subtraction:
                operation = CharacterOperation.SUBTRACTION;
                break;
            case R.id.btn_multiplication:
                operation = CharacterOperation.MULTIPLICATION;
                break;
            case R.id.btn_division:
                operation = CharacterOperation.DIVISION;
                break;
            case R.id.btn_power:
                operation = CharacterOperation.POWER;
                break;
            case R.id.btn_factorial:
                operation = CharacterOperation.FACTORIAL;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }

        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        insertExpression(expressionBuilder, operation);
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEditText(expressionBuilder);

        calculate();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClickInsertOperationWithBrackets(View view) {
        Object operation;

        switch (view.getId()) {
            case R.id.btn_square_root:
                operation = CharacterOperation.SQUARE_ROOT;
                break;
            case R.id.btn_sine:
                operation = StringOperation.SINE;
                break;
            case R.id.btn_cosine:
                operation = StringOperation.COSINE;
                break;
            case R.id.btn_tangent:
                operation = StringOperation.TANGENT;
                break;
            case R.id.btn_logarithm:
                operation = StringOperation.LOGARITHM;
                break;
            case R.id.btn_natural_logarithm:
                operation = StringOperation.NATURAL_LOGARITHM;
                break;
            case R.id.btn_absolute:
                operation = StringOperation.ABSOLUTE;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }

        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());

        insertExpression(expressionBuilder, Symbol.OPEN_BRACKET);
        insertExpression(expressionBuilder, operation);
        formatExpression(expressionBuilder);
        insertCommasInNumbers(expressionBuilder);
        insertExpressionToEditText(expressionBuilder);
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
        insertExpressionToEditText(expressionBuilder);

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
        insertExpressionToEditText(expressionBuilder);

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

    private void insertExpressionToEditText(StringBuilder expressionBuilder) {
        int selectionEnd = inputExpression.getSelectionEnd();
        int lengthBeforeInsert = inputExpression.length();
        int lengthAfterInsert = expressionBuilder.length();

        inputExpression.setText(expressionBuilder);

        if (inputExpression.isFocused()) {
            int moveCursor = lengthAfterInsert - lengthBeforeInsert;

            if (moveCursor + selectionEnd < 0) {
                moveCursor = selectionEnd = 0;
            }

            inputExpression.setSelection(selectionEnd + moveCursor);
        } else {
            inputExpression.setSelection(lengthAfterInsert);
        }
    }

    private boolean isCharacterOperation(char character) {
        for (CharacterOperation operation : CharacterOperation.values()) {
            if (isCharacterOperation(character, operation)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCharacterOperation(char character, CharacterOperation operation) {
        return String.valueOf(character).equals(operation.getDisplayValue());
    }

    private boolean isNumber(char character) {
        for (Number number : Number.values()) {
            if (isNumber(character, number)) {
                return true;
            }
        }

        return isNumeric(character);
    }

    private boolean isNumber(char character, Number number) {
        return String.valueOf(character).equals(number.toString());
    }

    private boolean isNumeric(char character) {
        return Character.isDigit(character) || character == Symbol.DOT.getValue();
    }

    private void insertCommasInNumbers(StringBuilder expression) {
        StringBuilder expressionBuilder = new StringBuilder();
        int digitCount = 0;
        int decimalPointIdx = findNextDecimalPoint(expression, expression.length() - 1);
        boolean numberStarted = false;

        for (int i = expression.length() - 1; i >= 0; --i) {
            if (expression.charAt(i) != Symbol.COMMA.getValue()) {
                if (Character.isDigit(expression.charAt(i))) {
                    if (!numberStarted) {
                        decimalPointIdx = findNextDecimalPoint(expression, i);
                        numberStarted = true;
                    }
                    if (decimalPointIdx == -1 && ++digitCount == 4) {
                        expressionBuilder.insert(0, Symbol.COMMA.getValue());
                        digitCount = 1;
                    }
                } else {
                    digitCount = 0;
                    numberStarted = false;
                }

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
        boolean isDecimal = false;
        int start = inputExpression.getSelectionStart();
        int end = inputExpression.getSelectionEnd();

        if (start == end) {
            ++end;
        }

        for (int i = 0; i >= 0 && i < expressionBuilder.length(); ++i) {
            char character = expressionBuilder.charAt(i);
            if (i > 0) {
                previousCharacter = expressionBuilder.charAt(i - 1);
            } else {
                previousCharacter = Constant.EMPTY_CHAR_VALUE;
            }

            if (!inputExpression.isFocused()) {
                start = i;
                end = i + 1;
            }

            if (character != Symbol.COMMA.getValue()) {
                if (character == Symbol.DOT.getValue()) {
                    if (!isDecimal) {
                        if (!isNumeric(previousCharacter)) {
                            expressionBuilder.insert(i, Number.ZERO);
                            --i;
                        } else {
                            isDecimal = true;
                        }
                        numberIdx = 0;
                    } else {
                        expressionBuilder.replace(i, i + 1, Constant.EMPTY_STRING);
                        --i;
                    }
                } else {
                    if (isNumber(character)) {
                        if (isNumber(character, Number.PI) || isNumber(character, Number.EULER)) {
                            if (isNumber(previousCharacter) || previousCharacter == Symbol.CLOSED_BRACKET.getValue()
                                    || isCharacterOperation(previousCharacter, CharacterOperation.FACTORIAL)) {
                                expressionBuilder.insert(i, CharacterOperation.MULTIPLICATION);
                            }
                        } else {
                            if (previousCharacter == Symbol.CLOSED_BRACKET.getValue()
                                    || isCharacterOperation(previousCharacter, CharacterOperation.FACTORIAL)
                                    || isNumber(previousCharacter, Number.PI)
                                    || isNumber(previousCharacter, Number.EULER)) {
                                expressionBuilder.insert(i, CharacterOperation.MULTIPLICATION);
                            }
                            if (!isDecimal && numberIdx == 1 && isNumber(previousCharacter, Number.ZERO)) {
                                expressionBuilder.replace(i - 1, i, Constant.EMPTY_STRING);
                                i -= 2;
                                --numberIdx;
                            } else if (isDecimal && numberIdx == Constant.MAX_DECIMAL_LENGTH) {
                                expressionBuilder.replace(start, end, Constant.EMPTY_STRING);
                                i -= end - start;
                                showToast(getResources().getString(R.string.max_decimal_length, String.valueOf(Constant.MAX_DECIMAL_LENGTH)));
                            } else if (!isDecimal && numberIdx == Constant.MAX_DIGIT_LENGTH) {
                                expressionBuilder.replace(start, end, Constant.EMPTY_STRING);
                                i -= end - start;
                                showToast(getResources().getString(R.string.max_number_length, String.valueOf(Constant.MAX_DIGIT_LENGTH)));
                            } else {
                                ++numberIdx;
                            }
                        }
                    } else {
                        numberIdx = 0;
                        isDecimal = false;

                        if (isCharacterOperation(character)) {
                            if (isCharacterOperation(character, CharacterOperation.SQUARE_ROOT)) {
                                if (isNumber(previousCharacter) || isCharacterOperation(previousCharacter, CharacterOperation.FACTORIAL)
                                        || previousCharacter == Symbol.CLOSED_BRACKET.getValue()) {
                                    expressionBuilder.insert(i, CharacterOperation.MULTIPLICATION);
                                }
                            } else {
                                if (isCharacterOperation(previousCharacter) && !isCharacterOperation(previousCharacter, CharacterOperation.FACTORIAL)) {
                                    expressionBuilder.replace(i - 1, i, Constant.EMPTY_STRING);
                                    i -= 2;
                                } else if ((previousCharacter == Constant.EMPTY_CHAR_VALUE || previousCharacter == Symbol.OPEN_BRACKET.getValue())
                                        && !isCharacterOperation(character, CharacterOperation.SUBTRACTION)) {
                                    expressionBuilder.replace(i, i + 1, Constant.EMPTY_STRING);
                                    --i;
                                }
                            }
                        } else if (character != Symbol.CLOSED_BRACKET.getValue() && (previousCharacter == Symbol.CLOSED_BRACKET.getValue() || isNumber(previousCharacter))) {
                            expressionBuilder.insert(i, CharacterOperation.MULTIPLICATION);
                        }
                    }
                }
            }
        }
    }

    private void trimExcessDecimals(StringBuilder expressionBuilder) {
        int decimalDotIdx = expressionBuilder.lastIndexOf(Symbol.DOT.toString());

        if (decimalDotIdx != -1) {
            for (int i = decimalDotIdx + 1; i < expressionBuilder.length(); ++i) {
                if (!isNumber(expressionBuilder.charAt(i), Number.ZERO)) {
                    return;
                }
            }

            expressionBuilder.replace(decimalDotIdx, expressionBuilder.length(), Constant.EMPTY_STRING);
        }
    }

    private void convertToCalculableExpression(StringBuilder expressionBuilder) {
        String expression = expressionBuilder.toString();

        for (CharacterOperation operation : CharacterOperation.values()) {
            expression = expression.replace(operation.getDisplayValue(), operation.getValue());
        }

        for (Number number : Number.values()) {
            expression = expression.replace(number.getDisplayValue(), number.getValue());
        }

        expression = expression.replace(Symbol.COMMA.toString(), Constant.EMPTY_STRING);

        expressionBuilder.replace(0, expressionBuilder.length(), expression);
    }

    private void calculate() {
        StringBuilder expressionBuilder = new StringBuilder(inputExpression.getText());
        convertToCalculableExpression(expressionBuilder);

        Expression expressionCalculation = new Expression(expressionBuilder.toString());

        if (expressionCalculation.checkSyntax()) {
            BigDecimal bigDecimal = BigDecimal.valueOf(expressionCalculation.calculate());
            double result = bigDecimal.setScale(Constant.MAX_DECIMAL_LENGTH, RoundingMode.HALF_UP).doubleValue();

            StringBuilder resultBuilder = new StringBuilder(String.valueOf(result));
            trimExcessDecimals(resultBuilder);
            insertCommasInNumbers(resultBuilder);

            resultExpression.setText(resultBuilder);
        }
    }
}