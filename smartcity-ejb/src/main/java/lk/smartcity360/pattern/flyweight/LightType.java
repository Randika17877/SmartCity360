package lk.smartcity360.pattern.flyweight;

import java.util.Arrays;

/** Flyweight: the immutable, shared (intrinsic) description and 24-hour dimming profile of a streetlight. */
public final class LightType {

    private final String model;
    private final int wattage;
    private final String colour;
    private final int[] dimmingProfile; // 24-hour profile (indices 0..23)

    LightType(String model, int wattage, String colour) {
        this(model, wattage, colour, LightTypeFactory.defaultProfile(wattage));
    }

    LightType(String model, int wattage, String colour, int[] dimmingProfile) {
        this.model = model;
        this.wattage = wattage;
        this.colour = colour;
        if (dimmingProfile == null || dimmingProfile.length != 24) {
            this.dimmingProfile = LightTypeFactory.defaultProfile(wattage);
        } else {
            this.dimmingProfile = Arrays.copyOf(dimmingProfile, 24);
        }
    }

    public String getModel() { return model; }

    public int getWattage() { return wattage; }

    public String getColour() { return colour; }

    public int[] getDimmingProfile() { return Arrays.copyOf(dimmingProfile, 24); }

    public int getBrightness(int hour) {
        int h = Math.floorMod(hour, 24);
        return dimmingProfile[h];
    }

    public String getDisplayConfig() {
        return model + ", " + wattage + "W, " + colour;
    }

    public String getHexId() {
        return "#" + Integer.toHexString(System.identityHashCode(this));
    }

    /** Describes one light of this type; the location and state are supplied by the caller. */
    public String render(String location, boolean on) {
        return String.format("%s %dW (%s) at %s is %s", model, wattage, colour, location, on ? "ON" : "OFF");
    }

    @Override
    public String toString() {
        return getDisplayConfig() + " (" + getHexId() + ")";
    }
}
