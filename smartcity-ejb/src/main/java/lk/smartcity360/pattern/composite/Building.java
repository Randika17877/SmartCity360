package lk.smartcity360.pattern.composite;

import java.util.List;

/** Leaf: a building with an energy usage reading and power state. */
public class Building implements CityComponent {

    private final String name;
    private double usage;
    private boolean powerOn;

    public Building(String name) {
        this(name, 0.0, true);
    }

    public Building(String name, double usage, boolean powerOn) {
        this.name = name;
        this.usage = usage;
        this.powerOn = powerOn;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getUsage() {
        return usage;
    }

    public void setUsage(double usage) {
        this.usage = usage;
    }

    @Override
    public void setPower(boolean on) {
        this.powerOn = on;
    }

    @Override
    public boolean isPowerOn() {
        return powerOn;
    }

    @Override
    public List<String> execute(String command) {
        if ("SHUTDOWN_POWER".equalsIgnoreCase(command) || "SHUTDOWN".equalsIgnoreCase(command) || "OFF".equalsIgnoreCase(command)) {
            setPower(false);
        } else if ("RESTORE_POWER".equalsIgnoreCase(command) || "RESTORE".equalsIgnoreCase(command) || "ON".equalsIgnoreCase(command)) {
            setPower(true);
        }
        return List.of(command + " executed at building " + name);
    }
}
