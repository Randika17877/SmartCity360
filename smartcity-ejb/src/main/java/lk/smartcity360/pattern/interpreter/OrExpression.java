package lk.smartcity360.pattern.interpreter;

/** Non-terminal expression: true when at least one operand is true. */
public class OrExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean interpret(PolicyContext context) {
        return left.interpret(context) || right.interpret(context);
    }
}
