package lk.smartcity360.pattern.interpreter;

/** Non-terminal expression: true when both operands are true. */
public class AndExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public AndExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() { return left; }
    public Expression getRight() { return right; }

    @Override
    public boolean interpret(PolicyContext context) {
        return left.interpret(context) && right.interpret(context);
    }

    @Override
    public String toTreeString() {
        return "(" + left.toTreeString() + " AND " + right.toTreeString() + ")";
    }

    @Override
    public String toString() {
        return toTreeString();
    }
}
