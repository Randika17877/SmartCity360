package lk.smartcity360.web;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.service.ComplaintService;

@Named
@RequestScoped
public class ComplaintBean {

    @EJB
    private ComplaintService service;

    private Complaint complaint = new Complaint();

    public Complaint getComplaint() { return complaint; }

    public List<Complaint> getComplaints() { return service.all(); }

    public void submit() {
        try {
            service.submit(complaint);
            FacesMessages.info("Complaint routed to " + complaint.getAssignedLevel().getLabel());
            complaint = new Complaint();
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }

    public void resolve(Long id) {
        try {
            service.resolve(id);
        } catch (RuntimeException e) {
            FacesMessages.error(e);
        }
    }
}
