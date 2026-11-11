package lk.smartcity360.entity;

public enum ComplaintStatus {
    NEW("New"),
    ASSIGNED("Assigned"),
    ESCALATED("Escalated"),
    HIGH_PRIORITY("High priority"),
    MANUAL_REVIEW("Manual review"),
    RESOLVED("Resolved");

    private final String label;

    ComplaintStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
