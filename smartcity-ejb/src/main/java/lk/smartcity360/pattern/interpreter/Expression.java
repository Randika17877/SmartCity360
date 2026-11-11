package lk.smartcity360.pattern.interpreter;

/** Abstract expression: every node of a parsed policy can be evaluated against a context. */
public interface Expression {

    boolean interpret(PolicyContext context);
}
