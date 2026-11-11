package lk.smartcity360.pattern.composite;

import java.util.List;

/** Component: a building (leaf) or any group of city nodes (composite). */
public interface CityComponent {

    String getName();

    /** Runs a command on this node and everything below it; returns one log line per node reached. */
    List<String> execute(String command);

    /** Calculates total energy usage at this level (leaves return their reading, composites sum children). */
    double getUsage();

    /** Switches power state on this node and propagates recursively to all descendants. */
    void setPower(boolean on);

    /** Returns whether power is on. */
    boolean isPowerOn();
}
