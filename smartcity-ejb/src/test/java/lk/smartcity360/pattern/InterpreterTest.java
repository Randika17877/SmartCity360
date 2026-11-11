package lk.smartcity360.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import lk.smartcity360.pattern.interpreter.Expression;
import lk.smartcity360.pattern.interpreter.PolicyContext;
import lk.smartcity360.pattern.interpreter.PolicyParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InterpreterTest {

    private final PolicyParser parser = new PolicyParser();

    private boolean eval(String rule, double noise, double hour) {
        return parser.parse(rule).interpret(new PolicyContext(Map.of("noiseDb", noise, "hour", hour)));
    }

    @Test
    void sampleRuleFromTheAssignment() {
        String rule = "noiseDb > 60 AND hour >= 22";

        assertTrue(eval(rule, 65, 22));
        assertFalse(eval(rule, 65, 21));
        assertFalse(eval(rule, 60, 23));
    }

    @Test
    void municipalOrdinancesAndTreeStrings() {
        // Ordinance 1: Night Quiet Zone Regulation
        String r1 = "(hour >= 23 OR hour < 6) AND soundLevel <= 45 OR ((hour >= 6 AND hour < 23) AND soundLevel <= 65)";
        Expression e1 = parser.parse(r1);
        assertEquals("(((hour >= 23 OR hour < 6) AND soundLevel <= 45) OR ((hour >= 6 AND hour < 23) AND soundLevel <= 65))", e1.toTreeString());
        assertTrue(e1.interpret(new PolicyContext(Map.of("hour", 23.0, "soundLevel", 40.0)))); // Night quiet
        assertFalse(e1.interpret(new PolicyContext(Map.of("hour", 23.0, "soundLevel", 62.0)))); // Night loud (violation)
        assertTrue(e1.interpret(new PolicyContext(Map.of("hour", 14.0, "soundLevel", 62.0)))); // Daytime compliant

        // Ordinance 2: Arterial Commercial Access
        String r2 = "transitPass == 1 OR hour >= 8 AND hour < 19";
        Expression e2 = parser.parse(r2);
        assertEquals("(transitPass == 1 OR (hour >= 8 AND hour < 19))", e2.toTreeString());
        assertTrue(e2.interpret(new PolicyContext(Map.of("transitPass", 1.0, "hour", 21.0)))); // Has transit pass
        assertTrue(e2.interpret(new PolicyContext(Map.of("transitPass", 0.0, "hour", 12.0)))); // Daytime hours
        assertFalse(e2.interpret(new PolicyContext(Map.of("transitPass", 0.0, "hour", 22.0)))); // Outside hours without pass

        // Ordinance 3: Low Emission Transit Corridor
        String r3 = "emissionTier >= 4 OR NOT (hour >= 7 AND hour < 10)";
        Expression e3 = parser.parse(r3);
        assertEquals("(emissionTier >= 4 OR NOT (hour >= 7 AND hour < 10))", e3.toTreeString());
        assertTrue(e3.interpret(new PolicyContext(Map.of("emissionTier", 5.0, "hour", 8.0)))); // Clean vehicle allowed in morning rush
        assertFalse(e3.interpret(new PolicyContext(Map.of("emissionTier", 2.0, "hour", 8.0)))); // High emission vehicle restricted in morning rush
        assertTrue(e3.interpret(new PolicyContext(Map.of("emissionTier", 2.0, "hour", 13.0)))); // Allowed outside rush hours
    }

    @Test
    void everyOperatorWorks() {
        assertTrue(eval("noiseDb > 60", 61, 0));
        assertTrue(eval("noiseDb >= 60", 60, 0));
        assertTrue(eval("noiseDb < 60", 59, 0));
        assertTrue(eval("noiseDb <= 60", 60, 0));
        assertTrue(eval("noiseDb == 60", 60, 0));
        assertTrue(eval("noiseDb != 60", 61, 0));
        assertFalse(eval("noiseDb != 60", 60, 0));
    }

    @Test
    void keywordsAreCaseInsensitiveAndWhitespaceIsFlexible() {
        assertTrue(eval("  noiseDb>60   and   hour>=22 ", 70, 23));
    }

    @Test
    void andBindsTighterThanOr() {
        // hour == 3 OR (noiseDb > 80 AND hour > 20)
        String rule = "hour == 3 OR noiseDb > 80 AND hour > 20";

        assertTrue(eval(rule, 10, 3));
        assertTrue(eval(rule, 90, 21));
        assertFalse(eval(rule, 90, 10));
    }

    @Test
    void chainsOfThreeOrMoreClausesAreSupported() {
        assertTrue(eval("noiseDb > 1 AND noiseDb > 2 AND hour > 3", 5, 5));
        assertFalse(eval("noiseDb > 1 AND noiseDb > 2 AND hour > 3", 5, 1));
    }

    @Test
    void negativeAndDecimalNumbers() {
        assertTrue(eval("noiseDb > -1.5", 0, 0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "noiseDb", "noiseDb >", "> 60", "noiseDb ~ 60", "noiseDb > sixty", "noiseDb > 60 AND"})
    void malformedRulesAreRejected(String rule) {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(rule));
    }

    @Test
    void unknownVariableIsAnErrorNotSilentlyZero() {
        Expression_ rule = new Expression_();
        assertThrows(IllegalArgumentException.class, rule::run);
    }

    /** Tiny holder so the lambda target reads clearly. */
    private final class Expression_ {
        void run() {
            parser.parse("pressure > 1").interpret(new PolicyContext(Map.of("noiseDb", 1.0)));
        }
    }

    @Test
    void nullRuleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
        assertEquals("Unknown variable: x", assertThrows(IllegalArgumentException.class,
                () -> new PolicyContext(Map.of()).get("x")).getMessage());
    }
}
