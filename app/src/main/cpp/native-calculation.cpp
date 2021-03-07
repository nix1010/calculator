//
// Created by Nikola on 7.3.2021.
//
#include <cmath>
#include <cstdio>
#include <cctype>
#include <jni.h>

#define SIZE 500
#define NUMBER_SIZE 30
#define GREATEST_PRIORITY 4

static const char *input;
static char number[NUMBER_SIZE];
static const char *ptr;

static double result = 0;
static int number_idx = 0, expr_idx = 0, brackets = 0;

class Expression {
protected:
    Expression *parent;
    unsigned int priority;
public:
    static unsigned int max_priority;

    virtual ~Expression() = default;

    explicit Expression(unsigned int priority) : parent(nullptr) {
        setPriority(priority);
    }

    virtual double calculate() const = 0;

    void setParent(Expression *parent) {
        this->parent = parent;
    }

    Expression *findGreatestParent() {
        Expression *expr = this;

        while (expr->parent) {
            expr = expr->parent;
        }

        return expr;
    }

    void setPriority(unsigned int priority) {
        this->priority = priority;

        if (max_priority < this->priority) {
            max_priority = this->priority;
        }
    }

    unsigned int getPriority() const {
        return priority;
    }

    static unsigned int getDefaultPriority(char symbol = '\0') {
        switch (symbol) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
            case '%':
                return 2;
            case '^':
                return 3;
            case 'r':
            case '!':
            case 'c':
            case 's':
            case 't':
            case 'k':
                return 4;
            default:
                return 0;
        }
    }
};

unsigned int Expression::max_priority = 0;

class Number : public Expression {
    double value;
public:
    ~Number() override = default;

    explicit Number(double value) : Expression(0), value(value) {}

    double calculate() const override {
        return value;
    }
};

Expression *expr_arr[SIZE];

class Operation : public Expression {
    const char symbol;
    Expression *attribute1;
    Expression *attribute2;
public:
    ~Operation() override = default;

    explicit Operation(char symbol) : Expression(Expression::getDefaultPriority(symbol)),
                                      symbol(symbol),
                                      attribute1(nullptr), attribute2(nullptr) {}

    bool isAttributeSetable(Expression *attribute) {
        if (attribute) {
            // Ako je attribute broj
            // ili ako je operacija onda prioritet atributa mora biti veci ili jednak
            // i nesme biti roditelj trenutnom izrazu (cirkluarno izbegavanje)
            if (attribute->getPriority() == 0 ||
                (attribute->getPriority() >= this->getPriority() && this->parent != attribute)) {
                return true;
            }
        }

        return false;
    }

    //----- POSTAVLJANJE ATRIBUTA -----
    void setAtribute1(Expression *attribute1) {
        if (attribute1) {
            this->attribute1 = attribute1;
            attribute1->setParent(this);
        }
    }

    void setAtribute2(Expression *attribute2) {
        if (attribute2) {
            this->attribute2 = attribute2;
            attribute2->setParent(this);
        }
    }

    //----- ALGORITAM RACUNANJA -----
    double calculate() const override {
        switch (symbol) {
            case '+':
                if (!attribute1 && attribute2) {
                    return result + attribute2->calculate();
                } else if (!attribute1 && !attribute2) {
                    return result;
                } else if (attribute1 && !attribute2) {
                    return attribute1->calculate();
                }

                return attribute1->calculate() + attribute2->calculate();

            case '-':
                //std::cout << "START\n";
                if (!attribute1 && attribute2) {
                    if (priority > Expression::getDefaultPriority(
                            this->symbol)) { //ako se racuna da je broj negativan
                        return attribute2->calculate() * -1;
                    }
                    return result -
                           attribute2->calculate(); //ako je na prvo mesto minus oduzmi od zadnji result
                } else if (!attribute1 && !attribute2) {
                    return result;
                } else if (attribute1 && !attribute2) {
                    return attribute1->calculate();
                }

                return attribute1->calculate() - attribute2->calculate();

            case '*':
                if (!attribute1 && attribute2) {
                    return result * attribute2->calculate();
                } else if (!attribute1 && !attribute2) {
                    return result;
                } else if (attribute1 && !attribute2) {
                    return attribute1->calculate();
                }

                return attribute1->calculate() * attribute2->calculate();

            case '/':
                if (!attribute1 && attribute2) {
                    return result / attribute2->calculate();
                } else if (!attribute1 && !attribute2) {
                    return result;
                } else if (attribute1 && !attribute2) {
                    return attribute1->calculate();
                }

                return attribute1->calculate() / attribute2->calculate();

            case '%': {
                long long atr1 = 0, atr2 = 0, mod_res;

                if (!attribute1 && attribute2) {
                    atr1 = (long long) result;
                    atr2 = (long long) attribute2->calculate();
                } else if (!attribute1 && !attribute2) {
                    return result;
                } else if (attribute1 && !attribute2) {
                    return attribute1->calculate();
                } else {
                    atr1 = (long long) attribute1->calculate();
                    atr2 = (long long) attribute2->calculate();
                }

                mod_res = atr1 % atr2;

                if ((atr1 < 0 && atr2 >= 0) || (atr1 >= 0 && atr2 < 0)) {
                    mod_res += atr2;
                }

                return (double) mod_res;
            }
            case '!': {
                long long f = 1, atr1 = 0;

                if (!this->attribute1) {
                    atr1 = (long long) result;
                } else {
                    atr1 = (long long) this->attribute1->calculate();
                }

                while (atr1) {
                    f *= atr1;
                    --atr1;
                }

                return (double) f;
            }
            case '^':
                if (!attribute1 && attribute2) {
                    return pow(result, attribute2->calculate());
                } else if (!attribute1 && !attribute2) {
                    return 0;
                } else if (attribute1 && !attribute2) {
                    return attribute1->calculate();
                }

                return pow(attribute1->calculate(), attribute2->calculate());

            case 'r':
                if (!attribute1 && !attribute2) {
                    return pow(result, 0.5);
                } else if (!attribute1 && attribute2) {
                    return pow(attribute2->calculate(), 0.5);
                } else if (attribute1 && !attribute2) {
                    return pow(result, 1 / attribute1->calculate());
                }

                return pow(attribute2->calculate(), 1 / attribute1->calculate());

            case 'c':
                if (!attribute2) {
                    return cos(result);
                }

                return cos(attribute2->calculate());

            case 's':
                if (!attribute2) {
                    return sin(result);
                }

                return sin(attribute2->calculate());

            case 't':
                if (!attribute2) {
                    return tan(result);
                }

                return tan(attribute2->calculate());

            case 'k':
                if (!attribute2) {
                    return 1 / tan(result);
                }

                return 1 / tan(attribute2->calculate());

            default:
                return 0;
        }
    }
};

static double convertBufferToDouble() {
    double num = atof(number);
    number[0] = '\0';
    number_idx = 0;

    return num;
}

static void saveCharInBuffer() {
    number[number_idx++] = *ptr;
    number[number_idx] = '\0';
}

bool isDigit(char character) {
    return isdigit(character) || character == '.';
}

bool isSkippable(char character) {
    return character == ' ' || character == ',';
}

void reset() {
    for (int i = 0; i < expr_idx; ++i) {
        delete expr_arr[i];
    }
    Expression::max_priority = brackets = expr_idx = number_idx = 0;
}

extern "C" JNIEXPORT jdouble JNICALL Java_com_example_calculator_MainActivity_calculate(JNIEnv *env,
                                                            jclass,
                                                            jstring expression) {

    input = env->GetStringUTFChars(expression, nullptr);
    ptr = input;
    result = 0;

    //----- GENERISANJE TIPOVA OPERACIJA -----
    while (*ptr) {
        if (isDigit(*ptr)) {
            saveCharInBuffer();
        }
        if ((!(isDigit(*ptr)) && !isSkippable(*ptr)) || *(ptr + 1) == '\0') {
            if (number_idx) {
                expr_arr[expr_idx++] = new Number(convertBufferToDouble());
            }
            char symbol = '\0';

            switch (*ptr) {
                case '(':
                    ++brackets;
                    break;
                case ')':
                    if (--brackets < 0) {
                        brackets = 0;
                    }
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                case '%':
                case '^':
                case '!':
                case 'r':
                    symbol = *ptr;
                    break;
                case 'c':
                    if (((ptr - input) + 2 < SIZE)) {
                        if (*(ptr + 1) == 'o' && (*(ptr + 2) == 's')) {
                            symbol = *ptr;
                        } else if (*(ptr + 1) == 'o' && (*(ptr + 2) == 't')) {
                            symbol = 'k';
                        }
                    }
                    break;
                case 's':
                    if (((ptr - input) + 2 < SIZE) && (*(ptr + 1) == 'i') && (*(ptr + 2) == 'n')) {
                        symbol = *ptr;
                    }
                    break;
                case 't':
                    if (((ptr - input) + 2 < SIZE) && (*(ptr + 1) == 'a') && (*(ptr + 2) == 'n')) {
                        symbol = *ptr;
                    }
                    break;
            }
            if (symbol) {
                expr_arr[expr_idx++] = new Operation(symbol);
                if (brackets > 0) {
                    auto *operation = (Operation *) expr_arr[expr_idx - 1];
                    operation->setPriority(operation->getPriority() + GREATEST_PRIORITY * brackets);
                }
            }
        }
        ++ptr;
    }

    //----- ALGORITAM PRIORITETA -----
    while (Expression::max_priority) {
        for (int i = 0; i < expr_idx; ++i) {
            Operation *operation = nullptr;

            if (expr_arr[i]->getPriority() > 0) {
                operation = (Operation *) expr_arr[i];
            }
            if (operation && operation->getPriority() == Expression::max_priority) {
                if (i - 1 >= 0) {
                    Expression *attribute = expr_arr[i - 1]->findGreatestParent();

                    if (operation->isAttributeSetable(attribute)) {
                        operation->setAtribute1(attribute);
                    }
                }
                if (i + 1 < expr_idx) {
                    Expression *attribute = expr_arr[i + 1]->findGreatestParent();

                    if (operation->isAttributeSetable(attribute)) {
                        operation->setAtribute2(attribute);
                    }
                }
            }
        }

        --Expression::max_priority;
    }

    //----- RACUNANJE -----
    if (expr_idx) {
        result = (*expr_arr)->findGreatestParent()->calculate();
    }

    reset();

    env->ReleaseStringUTFChars(expression, input);

    return result;
}