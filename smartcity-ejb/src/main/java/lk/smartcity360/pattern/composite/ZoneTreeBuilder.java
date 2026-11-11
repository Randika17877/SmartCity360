package lk.smartcity360.pattern.composite;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lk.smartcity360.entity.Zone;
import lk.smartcity360.entity.ZoneType;

/** Turns the flat, persisted {@link Zone} rows (linked by parent id) into a Composite tree. */
public final class ZoneTreeBuilder {

    private ZoneTreeBuilder() {
    }

    /** Builds every node and wires children to parents; returns the nodes keyed by zone id. */
    public static Map<Long, CityComponent> index(List<Zone> zones) {
        Map<Long, CityComponent> nodes = new LinkedHashMap<>();
        for (Zone zone : zones) {
            if (zone.getType() == ZoneType.BUILDING) {
                double usage = zone.getUsageKwh() != null ? zone.getUsageKwh() : 0.0;
                boolean power = zone.isPowerOn();
                nodes.put(zone.getId(), new Building(zone.getName(), usage, power));
            } else {
                nodes.put(zone.getId(), new ZoneGroup(zone.getName()));
            }
        }
        for (Zone zone : zones) {
            if (nodes.get(zone.getParentId()) instanceof ZoneGroup parent) {
                parent.add(nodes.get(zone.getId()));
            }
        }
        return nodes;
    }

    /** The root nodes: zones without a parent, or whose parent row no longer exists. */
    public static List<CityComponent> roots(List<Zone> zones) {
        Map<Long, CityComponent> nodes = index(zones);
        List<CityComponent> roots = new ArrayList<>();
        for (Zone zone : zones) {
            if (!(nodes.get(zone.getParentId()) instanceof ZoneGroup)) {
                roots.add(nodes.get(zone.getId()));
            }
        }
        return roots;
    }
}
