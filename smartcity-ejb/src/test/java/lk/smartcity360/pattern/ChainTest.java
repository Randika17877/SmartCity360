package lk.smartcity360.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lk.smartcity360.entity.AssignmentLevel;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.entity.ComplaintStatus;
import lk.smartcity360.pattern.chain.ComplaintHandler;
import lk.smartcity360.pattern.chain.LocalStaffHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChainTest {

    @ParameterizedTest
    @CsvSource({
        "1, LOCAL,        ASSIGNED",
        "2, LOCAL,        ASSIGNED",
        "3, SUPERVISOR,   ESCALATED",
        "4, SUPERVISOR,   ESCALATED",
        "5, CITY_MANAGER, HIGH_PRIORITY"
    })
    void severityDecidesWhoHandlesTheComplaint(int severity, AssignmentLevel level, ComplaintStatus status) {
        Complaint complaint = new Complaint("Resident", "Problem", severity);

        ComplaintHandler.standardChain().handle(complaint);

        assertEquals(level, complaint.getAssignedLevel());
        assertEquals(status, complaint.getStatus());
    }

    @Test
    void incompleteChainFallsBackToManualReview() {
        Complaint complaint = new Complaint("Resident", "Problem", 5);

        new LocalStaffHandler().handle(complaint);

        assertEquals(AssignmentLevel.UNASSIGNED, complaint.getAssignedLevel());
        assertEquals(ComplaintStatus.MANUAL_REVIEW, complaint.getStatus());
    }
}
