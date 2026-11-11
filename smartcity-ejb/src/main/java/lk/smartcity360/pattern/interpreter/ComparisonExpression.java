package lk.smartcity360.pattern.interpreter;

/** Terminal expression: {@code variable operator number}, for example {@code noiseDb > 60}. */
public class ComparisonExpression implements Expression {

    private final String variable;
    private final Operator operator;
    private final double target;

    public ComparisonExpression(String variable, Operator operator, double target) {
        this.variable = variable;
        this.operator = operator;
        this.target = target;
    }

    @Override
    public boolean interpret(PolicyContext context) {
        return operator.test(context.get(variable), target);
    }
}
