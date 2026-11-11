package lk.smartcity360.pattern.composite;

import java.util.List;

/** Leaf: a building. */
public class Building implements CityComponent {

    private final String name;

    public Building(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> execute(String command) {
        return List.of(command + " executed at building " + name);
    }
}
