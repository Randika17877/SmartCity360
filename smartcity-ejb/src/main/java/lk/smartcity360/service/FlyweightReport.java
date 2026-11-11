package lk.smartcity360.service;

import java.io.Serializable;
import java.util.List;

/** Result of rendering all streetlights through the Flyweight pool. */
public record FlyweightReport(List<String> lines, int lights, int sharedTypes) implements Serializable {

    public String summary() {
        return lights + " streetlights share " + sharedTypes + " light type object(s)";
    }
}
