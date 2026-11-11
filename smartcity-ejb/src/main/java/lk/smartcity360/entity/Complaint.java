package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** A citizen complaint. Assignment fields are set by the escalation chain on submission. */
@Entity
@Table(name = "complaints")
public class Complaint {

    public static final int MIN_SEVERITY = 1;
    public static final int MAX_SEVERITY = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Resident name is required")
    @Size(max = 150)
    @Column(name = "resident_name", nullable = false, length = 150)
    private String residentName;

    @NotBlank(message = "Description is required")
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String description;

    @Min(value = MIN_SEVERITY, message = "Severity must be between 1 and 5")
    @Max(value = MAX_SEVERITY, message = "Severity must be between 1 and 5")
    @Column(nullable = false)
    private int severity = MIN_SEVERITY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_level", nullable = false, length = 30)
    private AssignmentLevel assignedLevel = AssignmentLevel.UNASSIGNED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Complaint() {
    }

    public Complaint(String residentName, String description, int severity) {
        this.residentName = residentName;
        this.description = description;
        this.severity = severity;
    }

    public Long getId() { return id; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public AssignmentLevel getAssignedLevel() { return assignedLevel; }
    public void setAssignedLevel(AssignmentLevel assignedLevel) { this.assignedLevel = assignedLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
