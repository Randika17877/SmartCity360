package lk.smartcity360.pattern.flyweight;

/** Flyweight: the immutable, shared (intrinsic) description of a kind of streetlight. */
public final class LightType {

    private final String model;
    private final int wattage;
    private final String colour;

    LightType(String model, int wattage, String colour) {
        this.model = model;
        this.wattage = wattage;
        this.colour = colour;
    }

    public String getModel() { return model; }

    public int getWattage() { return wattage; }

    public String getColour() { return colour; }

    /** Describes one light of this type; the location and state are supplied by the caller. */
    public String render(String location, boolean on) {
        return String.format("%s %dW (%s) at %s is %s", model, wattage, colour, location, on ? "ON" : "OFF");
    }
}
