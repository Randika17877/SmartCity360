package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lk.smartcity360.entity.Complaint;
import lk.smartcity360.entity.ComplaintStatus;
import lk.smartcity360.pattern.chain.ComplaintHandler;

/** Records complaints and routes them with the escalation chain (Chain of Responsibility). */
@Stateless
public class ComplaintService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    public Complaint submit(Complaint complaint) {
        ComplaintHandler.standardChain().handle(complaint);
        em.persist(complaint);
        return complaint;
    }

    public Complaint resolve(Long id) {
        Complaint complaint = em.find(Complaint.class, id);
        if (complaint == null) {
            throw new BusinessRuleException("Complaint " + id + " does not exist");
        }
        complaint.setStatus(ComplaintStatus.RESOLVED);
        return complaint;
    }

    public List<Complaint> all() {
        return em.createQuery("SELECT c FROM Complaint c ORDER BY c.createdAt DESC, c.id DESC", Complaint.class)
                .getResultList();
    }

    public long pendingCount() {
        return em.createQuery("SELECT COUNT(c) FROM Complaint c WHERE c.status <> :resolved", Long.class)
                .setParameter("resolved", ComplaintStatus.RESOLVED)
                .getSingleResult();
    }
}
