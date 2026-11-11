package lk.smartcity360.pattern.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Composite: a city, district or street that forwards commands and aggregates usage across children. */
public class ZoneGroup implements CityComponent {

    private final String name;
    private final List<CityComponent> children = new ArrayList<>();

    public ZoneGroup(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void add(CityComponent component) {
        children.add(component);
    }

    public void remove(CityComponent component) {
        children.remove(component);
    }

    public List<CityComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public double getUsage() {
        double sum = 0.0;
        for (CityComponent child : children) {
            sum += child.getUsage();
        }
        return sum;
    }

    @Override
    public void setPower(boolean on) {
        for (CityComponent child : children) {
            child.setPower(on);
        }
    }

    @Override
    public boolean isPowerOn() {
        for (CityComponent child : children) {
            if (child.isPowerOn()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> execute(String command) {
        List<String> log = new ArrayList<>();
        log.add(command + " received by zone " + name);
        if ("SHUTDOWN_POWER".equalsIgnoreCase(command) || "SHUTDOWN".equalsIgnoreCase(command) || "OFF".equalsIgnoreCase(command)) {
            setPower(false);
        } else if ("RESTORE_POWER".equalsIgnoreCase(command) || "RESTORE".equalsIgnoreCase(command) || "ON".equalsIgnoreCase(command)) {
            setPower(true);
        }
        for (CityComponent child : children) {
            log.addAll(child.execute(command));
        }
        return log;
    }
}
