package lk.smartcity360.pattern.interpreter;

/** Non-terminal expression: logical negation of its child expression. */
public class NotExpression implements Expression {

    private final Expression inner;

    public NotExpression(Expression inner) {
        this.inner = inner;
    }

    public Expression getInner() { return inner; }

    @Override
    public boolean interpret(PolicyContext context) {
        return !inner.interpret(context);
    }

    @Override
    public String toTreeString() {
        return "NOT " + inner.toTreeString();
    }

    @Override
    public String toString() {
        return toTreeString();
    }
}
