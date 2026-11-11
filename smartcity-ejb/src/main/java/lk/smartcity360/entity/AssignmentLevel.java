package lk.smartcity360.entity;

public enum AssignmentLevel {
    LOCAL("Local staff"),
    SUPERVISOR("Supervisor"),
    CITY_MANAGER("City manager"),
    UNASSIGNED("Unassigned");

    private final String label;

    AssignmentLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
