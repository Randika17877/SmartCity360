package lk.smartcity360.pattern.flyweight;

import java.util.Objects;

/** Context: the per-light (extrinsic) state, plus a reference to its shared {@link LightType}. */
public class StreetLightContext {

    private String code;
    private final String location;
    private final LightType type;
    private boolean faulty;
    private boolean on;

    public StreetLightContext(String location, LightType type, boolean on) {
        this("SL-" + Integer.toHexString(location.hashCode() & 0xffff), location, type, false);
        this.on = on;
    }

    public StreetLightContext(String code, String location, LightType type, boolean faulty) {
        this.code = Objects.requireNonNull(code, "code");
        this.location = Objects.requireNonNull(location, "location");
        this.type = Objects.requireNonNull(type, "type");
        this.faulty = faulty;
        this.on = !faulty;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLocation() { return location; }

    public LightType getType() { return type; }

    public boolean isFaulty() { return faulty; }
    public void setFaulty(boolean faulty) {
        this.faulty = faulty;
        if (faulty) this.on = false;
    }

    public boolean isOn() { return on; }
    public void setOn(boolean on) {
        this.on = on;
        if (on) this.faulty = false;
    }

    public int getBrightness(int hour) {
        if (faulty || !on) return 0;
        return type.getBrightness(hour);
    }

    public String getBrightnessPercent(int hour) {
        return getBrightness(hour) + "%";
    }

    public String getState() {
        return faulty ? "FAULTY" : (on ? "OK" : "OFF");
    }

    public String display() {
        return type.render(location, on && !faulty);
    }
}
