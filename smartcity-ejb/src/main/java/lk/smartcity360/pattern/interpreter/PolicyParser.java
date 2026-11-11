package lk.smartcity360.pattern.interpreter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the policy mini-language into an {@link Expression} tree.
 *
 * <pre>
 * policy     := conjunction ( "OR" conjunction )*
 * conjunction:= comparison ( "AND" comparison )*
 * comparison := identifier operator number          operator := &gt; &gt;= &lt; &lt;= == !=
 * </pre>
 *
 * {@code AND} binds tighter than {@code OR}; keywords are case-insensitive and there are no
 * parentheses. Example: {@code noiseDb > 60 AND hour >= 22}.
 */
public class PolicyParser {

    private static final Pattern OR = Pattern.compile("\\s+OR\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern AND = Pattern.compile("\\s+AND\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMPARISON = Pattern.compile(
            "([A-Za-z_]\\w*)\\s*(>=|<=|==|!=|>|<)\\s*(-?\\d+(?:\\.\\d+)?)");

    public Expression parse(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Policy expression is empty");
        }
        Expression result = null;
        for (String conjunction : OR.split(source.trim())) {
            Expression term = parseConjunction(conjunction);
            result = result == null ? term : new OrExpression(result, term);
        }
        return result;
    }

    private Expression parseConjunction(String text) {
        Expression result = null;
        for (String comparison : AND.split(text.trim())) {
            Expression term = parseComparison(comparison);
            result = result == null ? term : new AndExpression(result, term);
        }
        return result;
    }

    private Expression parseComparison(String text) {
        Matcher m = COMPARISON.matcher(text.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "Expected 'variable operator number' but found '" + text.trim() + "'");
        }
        return new ComparisonExpression(m.group(1), Operator.fromSymbol(m.group(2)), Double.parseDouble(m.group(3)));
    }
}
