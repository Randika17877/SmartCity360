package lk.smartcity360.web;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.service.ComplaintService;

@Named
@RequestScoped
public class ComplaintBean {

    @EJB
    private ComplaintService service;

    @Inject
    private AuthBean authBean;

    private Complaint complaint = new Complaint();

    @PostConstruct
    public void init() {
        if (authBean != null && authBean.getCurrentUser() != null) {
            complaint.setResidentName(authBean.getCurrentUser().getFullName());
        }
    }

    public Complaint getComplaint() { return complaint; }

    public List<Complaint> getComplaints() { return service.all(); }

    public void submit() {
        try {
            service.submit(complaint);
            FacesMessages.info("Complaint routed to " + complaint.getAssignedLevel().getLabel());
            complaint = new Complaint();
            if (authBean != null && authBean.getCurrentUser() != null) {
                complaint.setResidentName(authBean.getCurrentUser().getFullName());
            }
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void resolve(Long id) {
        if (authBean == null || !authBean.isStaffOrAdmin()) {
            FacesMessages.error("Permission denied: Citizens cannot resolve complaints. Only municipal staff and administrators are authorized.");
            return;
        }
        try {
            service.resolve(id);
            FacesMessages.info("Complaint #" + id + " has been marked as Resolved");
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }
}
