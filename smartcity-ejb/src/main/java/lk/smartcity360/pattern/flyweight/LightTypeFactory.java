package lk.smartcity360.pattern.flyweight;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Flyweight factory: returns one shared {@link LightType} per distinct configuration. */
public final class LightTypeFactory {

    private static final Map<String, LightType> TYPES = new ConcurrentHashMap<>();

    private LightTypeFactory() {
    }

    public static LightType get(String model, int wattage, String colour) {
        return get(model, wattage, colour, defaultProfile(wattage));
    }

    public static LightType get(String model, int wattage, String colour, int[] dimmingProfile) {
        String key = (model + "|" + wattage + "|" + colour).toLowerCase();
        return TYPES.computeIfAbsent(key, k -> new LightType(model, wattage, colour, dimmingProfile));
    }

    public static int cachedTypes() {
        return TYPES.size();
    }

    public static Collection<LightType> allTypes() {
        return Collections.unmodifiableCollection(TYPES.values());
    }

    /** Clears the pool. Intended for tests. */
    public static void reset() {
        TYPES.clear();
    }

    /** Standard 24-hour dimming profile: 100W+ (e.g. 120W) is 100% at peak night, 60W is 80%. */
    public static int[] defaultProfile(int wattage) {
        int[] p = new int[24];
        for (int h = 0; h < 24; h++) {
            if (h >= 6 && h < 18) {
                p[h] = 0; // Daytime off
            } else if (h == 18) {
                p[h] = wattage >= 100 ? 70 : 50;
            } else if (h == 19) {
                p[h] = wattage >= 100 ? 90 : 70;
            } else if (h >= 20 && h <= 23) {
                p[h] = wattage >= 100 ? 100 : 80;
            } else { // 0 to 5
                p[h] = wattage >= 100 ? 50 : 30;
            }
        }
        return p;
    }
}
