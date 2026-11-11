package lk.smartcity360.pattern.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses the policy mini-language into an {@link Expression} tree.
 *
 * <pre>
 * expression := or_expr
 * or_expr    := and_expr ( "OR" and_expr )*
 * and_expr   := not_expr ( "AND" not_expr )*
 * not_expr   := "NOT" not_expr | primary
 * primary    := "(" expression ")" | comparison
 * comparison := identifier operator number          operator := &gt; &gt;= &lt; &lt;= == !=
 * </pre>
 *
 * {@code AND} binds tighter than {@code OR}; keywords are case-insensitive and brackets {@code ( )}
 * override precedence. Example: {@code (hour >= 22 OR hour < 6) AND noise <= 50}.
 */
public class PolicyParser {

    private enum TokenType {
        IDENT, NUMBER, OP, AND, OR, NOT, LPAREN, RPAREN
    }

    private static final class Token {
        final TokenType type;
        final String text;

        Token(TokenType type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public String toString() {
            return type + "(" + text + ")";
        }
    }

    public Expression parse(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Policy expression is empty");
        }
        List<Token> tokens = tokenize(source.trim());
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Policy expression is empty");
        }
        ParserState state = new ParserState(tokens);
        Expression expr = state.parseOr();
        if (state.hasMore()) {
            throw new IllegalArgumentException("Unexpected token after expression: '" + state.peek().text + "'");
        }
        return expr;
    }

    private List<Token> tokenize(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "("));
                i++;
            } else if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")"));
                i++;
            } else if (c == '>' || c == '<' || c == '=' || c == '!') {
                if (i + 1 < n && s.charAt(i + 1) == '=') {
                    tokens.add(new Token(TokenType.OP, s.substring(i, i + 2)));
                    i += 2;
                } else if (c == '>' || c == '<') {
                    tokens.add(new Token(TokenType.OP, String.valueOf(c)));
                    i++;
                } else {
                    throw new IllegalArgumentException("Expected comparison operator at position " + i + " but found '" + c + "'");
                }
            } else if (Character.isDigit(c) || (c == '-' && i + 1 < n && Character.isDigit(s.charAt(i + 1)))) {
                int start = i;
                if (c == '-') i++;
                while (i < n && Character.isDigit(s.charAt(i))) i++;
                if (i < n && s.charAt(i) == '.' && i + 1 < n && Character.isDigit(s.charAt(i + 1))) {
                    i++;
                    while (i < n && Character.isDigit(s.charAt(i))) i++;
                }
                tokens.add(new Token(TokenType.NUMBER, s.substring(start, i)));
            } else if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < n && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')) i++;
                String word = s.substring(start, i);
                String upper = word.toUpperCase(Locale.ROOT);
                if ("AND".equals(upper)) {
                    tokens.add(new Token(TokenType.AND, "AND"));
                } else if ("OR".equals(upper)) {
                    tokens.add(new Token(TokenType.OR, "OR"));
                } else if ("NOT".equals(upper)) {
                    tokens.add(new Token(TokenType.NOT, "NOT"));
                } else {
                    tokens.add(new Token(TokenType.IDENT, word));
                }
            } else {
                throw new IllegalArgumentException("Unexpected character in policy: '" + c + "'");
            }
        }
        return tokens;
    }

    private static final class ParserState {
        private final List<Token> tokens;
        private int pos = 0;

        ParserState(List<Token> tokens) {
            this.tokens = tokens;
        }

        boolean hasMore() {
            return pos < tokens.size();
        }

        Token peek() {
            if (!hasMore()) {
                throw new IllegalArgumentException("Unexpected end of expression");
            }
            return tokens.get(pos);
        }

        boolean check(TokenType type) {
            return hasMore() && tokens.get(pos).type == type;
        }

        Token consume(TokenType expected) {
            if (!hasMore()) {
                throw new IllegalArgumentException("Expected " + expected + " but reached end of expression");
            }
            Token t = tokens.get(pos);
            if (t.type != expected) {
                throw new IllegalArgumentException("Expected " + expected + " but found '" + t.text + "'");
            }
            pos++;
            return t;
        }

        Expression parseOr() {
            Expression left = parseAnd();
            while (check(TokenType.OR)) {
                consume(TokenType.OR);
                Expression right = parseAnd();
                left = new OrExpression(left, right);
            }
            return left;
        }

        Expression parseAnd() {
            Expression left = parseNot();
            while (check(TokenType.AND)) {
                consume(TokenType.AND);
                Expression right = parseNot();
                left = new AndExpression(left, right);
            }
            return left;
        }

        Expression parseNot() {
            if (check(TokenType.NOT)) {
                consume(TokenType.NOT);
                Expression inner = parseNot();
                return new NotExpression(inner);
            }
            return parsePrimary();
        }

        Expression parsePrimary() {
            if (check(TokenType.LPAREN)) {
                consume(TokenType.LPAREN);
                Expression inner = parseOr();
                consume(TokenType.RPAREN);
                return inner;
            }
            return parseComparison();
        }

        Expression parseComparison() {
            Token var = consume(TokenType.IDENT);
            Token op = consume(TokenType.OP);
            Token num = consume(TokenType.NUMBER);
            try {
                double target = Double.parseDouble(num.text);
                return new ComparisonExpression(var.text, Operator.fromSymbol(op.text), target);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number in comparison: '" + num.text + "'", e);
            }
        }
    }
}
