package lk.smartcity360.pattern.interpreter;

import java.util.Map;

/** Context: the variable values a policy is evaluated against. */
public class PolicyContext {

    private final Map<String, Double> values;

    public PolicyContext(Map<String, Double> values) {
        this.values = Map.copyOf(values);
    }

    /** @throws IllegalArgumentException if the policy refers to a variable that was not supplied */
    public double get(String name) {
        Double value = values.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Unknown variable: " + name);
        }
        return value;
    }
}
