package lk.smartcity360.pattern.chain;

import lk.smartcity360.entity.AssignmentLevel;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.entity.ComplaintStatus;

/** Handles minor complaints (severity 1-2). */
public class LocalStaffHandler extends ComplaintHandler {

    static final int MAX_SEVERITY = 2;

    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getSeverity() <= MAX_SEVERITY;
    }

    @Override
    protected void assign(Complaint complaint) {
        complaint.setAssignedLevel(AssignmentLevel.LOCAL);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
    }
}
