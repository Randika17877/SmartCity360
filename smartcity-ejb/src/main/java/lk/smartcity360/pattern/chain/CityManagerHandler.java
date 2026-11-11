package lk.smartcity360.pattern.chain;

import lk.smartcity360.entity.AssignmentLevel;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.entity.ComplaintStatus;

/** Last resort: takes everything that reached it (severity 5 and above). */
public class CityManagerHandler extends ComplaintHandler {

    @Override
    protected boolean canHandle(Complaint complaint) {
        return true;
    }

    @Override
    protected void assign(Complaint complaint) {
        complaint.setAssignedLevel(AssignmentLevel.CITY_MANAGER);
        complaint.setStatus(ComplaintStatus.HIGH_PRIORITY);
    }
}
