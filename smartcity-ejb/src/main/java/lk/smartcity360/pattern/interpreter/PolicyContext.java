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
        if (value != null) {
            return value;
        }
        // Case-insensitive lookup fallback
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        // Aliases for noise / sound measurements
        if ("noiseDb".equalsIgnoreCase(name) || "soundLevel".equalsIgnoreCase(name) || "noise".equalsIgnoreCase(name)) {
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                if ("soundLevel".equalsIgnoreCase(entry.getKey())
                        || "noiseDb".equalsIgnoreCase(entry.getKey())
                        || "noise".equalsIgnoreCase(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        throw new IllegalArgumentException("Unknown variable: " + name);
    }
}
