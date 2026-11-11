package lk.smartcity360.pattern.chain;

import lk.smartcity360.entity.AssignmentLevel;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.entity.ComplaintStatus;

/**
 * Handler: decides whether it can take a complaint and otherwise passes it along the chain.
 * If the end of the chain is reached with no taker, the complaint is flagged for manual review.
 */
public abstract class ComplaintHandler {

    private ComplaintHandler next;

    /** Links the next handler and returns it, so chains can be written fluently. */
    public ComplaintHandler setNext(ComplaintHandler next) {
        this.next = next;
        return next;
    }

    public final void handle(Complaint complaint) {
        if (canHandle(complaint)) {
            assign(complaint);
        } else if (next != null) {
            next.handle(complaint);
        } else {
            complaint.setAssignedLevel(AssignmentLevel.UNASSIGNED);
            complaint.setStatus(ComplaintStatus.MANUAL_REVIEW);
        }
    }

    protected abstract boolean canHandle(Complaint complaint);

    protected abstract void assign(Complaint complaint);

    /** The standard escalation order: local staff, then supervisor, then city manager. */
    public static ComplaintHandler standardChain() {
        ComplaintHandler first = new LocalStaffHandler();
        first.setNext(new SupervisorHandler()).setNext(new CityManagerHandler());
        return first;
    }
}
