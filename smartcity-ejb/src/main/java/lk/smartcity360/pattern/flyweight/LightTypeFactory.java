package lk.smartcity360.pattern.flyweight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Flyweight factory: returns one shared {@link LightType} per distinct model/wattage/colour. */
public final class LightTypeFactory {

    private static final Map<String, LightType> TYPES = new ConcurrentHashMap<>();

    private LightTypeFactory() {
    }

    public static LightType get(String model, int wattage, String colour) {
        String key = model + "|" + wattage + "|" + colour;
        return TYPES.computeIfAbsent(key, k -> new LightType(model, wattage, colour));
    }

    public static int cachedTypes() {
        return TYPES.size();
    }

    /** Clears the pool. Intended for tests. */
    public static void reset() {
        TYPES.clear();
    }
}
