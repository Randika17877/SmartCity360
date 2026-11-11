package lk.smartcity360.pattern.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Composite: a city, district or street that forwards commands to its children. */
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
    public List<String> execute(String command) {
        List<String> log = new ArrayList<>();
        log.add(command + " received by zone " + name);
        for (CityComponent child : children) {
            log.addAll(child.execute(command));
        }
        return log;
    }
}
