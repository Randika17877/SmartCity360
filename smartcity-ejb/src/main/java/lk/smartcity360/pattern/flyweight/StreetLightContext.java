package lk.smartcity360.pattern.flyweight;

import java.util.Objects;

/** Context: the per-light (extrinsic) state, plus a reference to its shared {@link LightType}. */
public class StreetLightContext {

    private final String location;
    private final LightType type;
    private boolean on;

    public StreetLightContext(String location, LightType type, boolean on) {
        this.location = location;
        this.type = Objects.requireNonNull(type, "type");
        this.on = on;
    }

    public LightType getType() { return type; }

    public void setOn(boolean on) { this.on = on; }

    public String display() {
        return type.render(location, on);
    }
}
