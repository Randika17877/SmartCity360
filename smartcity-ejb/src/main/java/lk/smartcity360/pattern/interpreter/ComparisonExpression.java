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

    public String getVariable() { return variable; }
    public Operator getOperator() { return operator; }
    public double getTarget() { return target; }

    @Override
    public boolean interpret(PolicyContext context) {
        return operator.test(context.get(variable), target);
    }

    @Override
    public String toTreeString() {
        String num = (target == (long) target) ? String.valueOf((long) target) : String.valueOf(target);
        return variable + " " + operator.getSymbol() + " " + num;
    }

    @Override
    public String toString() {
        return toTreeString();
    }
}
