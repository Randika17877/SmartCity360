package lk.smartcity360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "policy_history")
public class PolicyHistory {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_name", nullable = false, length = 150)
    private String policyName;

    @Column(name = "old_rule", nullable = false, length = 1000)
    private String oldRule;

    @Column(name = "new_rule", nullable = false, length = 1000)
    private String newRule;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public PolicyHistory() {}

    public PolicyHistory(String policyName, String oldRule, String newRule) {
        this.policyName = policyName;
        this.oldRule = oldRule;
        this.newRule = newRule;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public String getOldRule() { return oldRule; }
    public void setOldRule(String oldRule) { this.oldRule = oldRule; }

    public String getNewRule() { return newRule; }
    public void setNewRule(String newRule) { this.newRule = newRule; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getFormattedChangedAt() {
        return changedAt != null ? changedAt.format(FORMATTER) : "";
    }
}
