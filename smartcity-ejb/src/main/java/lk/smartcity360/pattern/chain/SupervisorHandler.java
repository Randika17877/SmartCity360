package lk.smartcity360.pattern.chain;

import lk.smartcity360.entity.AssignmentLevel;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.entity.ComplaintStatus;

/** Handles moderate complaints (severity 3-4). */
public class SupervisorHandler extends ComplaintHandler {

    static final int MAX_SEVERITY = 4;

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverity() <= MAX_SEVERITY;
    }

    @Override
    protected void assign(Complaint complaint) {
        complaint.setAssignedLevel(AssignmentLevel.SUPERVISOR);
        complaint.setStatus(ComplaintStatus.ESCALATED);
    }
}
