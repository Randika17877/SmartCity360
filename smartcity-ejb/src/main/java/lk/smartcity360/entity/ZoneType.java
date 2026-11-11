package lk.smartcity360.entity;

/**
 * Levels of the city hierarchy. Each level may only be attached to one kind of parent,
 * which is what keeps the persisted tree consistent with the Composite model.
 */
public enum ZoneType {
    CITY(null),
    DISTRICT(CITY),
    STREET(DISTRICT),
    BUILDING(STREET);

    private final ZoneType requiredParent;

    ZoneType(ZoneType requiredParent) {
        this.requiredParent = requiredParent;
    }

    /** The type a parent must have, or {@code null} when this type is a root. */
    public ZoneType requiredParent() {
        return requiredParent;
    }

    public boolean isRoot() {
        return requiredParent == null;
    }
}
