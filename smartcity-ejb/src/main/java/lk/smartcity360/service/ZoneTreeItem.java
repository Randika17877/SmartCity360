package lk.smartcity360.service;

import java.io.Serializable;
import java.util.Locale;
import lk.smartcity360.entity.ZoneType;

/** A flattened view-model representation of a composite tree node for hierarchical rendering. */
public class ZoneTreeItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private ZoneType type;
    private String level;
    private int depth;
    private int indentPx;
    private double usageKwh;
    private boolean powerOn;
    private boolean building;
    private boolean hasChildren;

    public ZoneTreeItem() {
    }

    public ZoneTreeItem(Long id, Long parentId, String name, ZoneType type, int depth, double usageKwh, boolean powerOn, boolean hasChildren) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.level = type != null ? toTitleCase(type.name()) : "";
        this.depth = depth;
        this.indentPx = depth * 26;
        this.usageKwh = usageKwh;
        this.powerOn = powerOn;
        this.building = type == ZoneType.BUILDING;
        this.hasChildren = hasChildren;
    }

    public ZoneTreeItem(Long id, String name, ZoneType type, int depth, double usageKwh, boolean powerOn) {
        this(id, null, name, type, depth, usageKwh, powerOn, type != ZoneType.BUILDING);
    }

    private static String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    public Long getId() { return id; }
    public Long getParentId() { return parentId; }
    public String getName() { return name; }
    public ZoneType getType() { return type; }
    public String getLevel() { return level; }
    public int getDepth() { return depth; }
    public int getIndentPx() { return indentPx; }
    public double getUsageKwh() { return usageKwh; }
    public String getUsageFormatted() { return String.format(Locale.US, "%.2f kWh", usageKwh); }
    public boolean isPowerOn() { return powerOn; }
    public boolean isBuilding() { return building; }
    public boolean isHasChildren() { return hasChildren; }
}
