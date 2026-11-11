package lk.smartcity360.pattern.interpreter;

import java.util.Arrays;

/** Comparison operators of the policy language. */
public enum Operator {
    GREATER_OR_EQUAL(">=") {
        @Override boolean test(double left, double right) { return left >= right; }
    },
    LESS_OR_EQUAL("<=") {
        @Override boolean test(double left, double right) { return left <= right; }
    },
    NOT_EQUAL("!=") {
        @Override boolean test(double left, double right) { return Double.compare(left, right) != 0; }
    },
    EQUAL("==") {
        @Override boolean test(double left, double right) { return Double.compare(left, right) == 0; }
    },
    GREATER(">") {
        @Override boolean test(double left, double right) { return left > right; }
    },
    LESS("<") {
        @Override boolean test(double left, double right) { return left < right; }
    };

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    abstract boolean test(double left, double right);

    public String symbol() {
        return symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Operator fromSymbol(String symbol) {
        return Arrays.stream(values())
                .filter(op -> op.symbol.equals(symbol))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported operator: " + symbol));
    }
}
