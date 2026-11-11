package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lk.smartcity360.entity.Zone;
import lk.smartcity360.entity.ZoneType;
import lk.smartcity360.pattern.composite.CityComponent;
import lk.smartcity360.pattern.composite.ZoneTreeBuilder;

/** Manages the city hierarchy (Composite pattern). */
@Stateless
public class CityService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    /**
     * Saves a zone after checking that its parent matches its type: a city has no parent, a
     * district sits under a city, a street under a district and a building under a street.
     */
    public Zone saveZone(Zone zone) {
        validateParent(zone);
        if (zone.getId() == null) {
            em.persist(zone);
            return zone;
        }
        return em.merge(zone);
    }

    public List<Zone> zones() {
        return em.createQuery("SELECT z FROM Zone z ORDER BY z.id", Zone.class).getResultList();
    }

    public long zoneCount() {
        return em.createQuery("SELECT COUNT(z) FROM Zone z", Long.class).getSingleResult();
    }

    /**
     * Runs a command on a zone and everything below it, using the Composite tree built from
     * the persisted rows.
     *
     * @return one log line per node reached
     */
    public List<String> broadcast(Long zoneId, String command) {
        if (command == null || command.isBlank()) {
            throw new BusinessRuleException("Command is required");
        }
        CityComponent node = ZoneTreeBuilder.index(zones()).get(zoneId);
        if (node == null) {
            throw new BusinessRuleException("Zone " + zoneId + " does not exist");
        }
        return node.execute(command.trim());
    }

    private void validateParent(Zone zone) {
        ZoneType type = zone.getType();
        Long parentId = zone.getParentId();

        if (type.isRoot()) {
            if (parentId != null) {
                throw new BusinessRuleException("A " + type + " cannot have a parent zone");
            }
            return;
        }
        if (parentId == null) {
            throw new BusinessRuleException("A " + type + " needs a parent " + type.requiredParent());
        }
        Zone parent = em.find(Zone.class, parentId);
        if (parent == null) {
            throw new BusinessRuleException("Parent zone " + parentId + " does not exist");
        }
        if (parent.getType() != type.requiredParent()) {
            throw new BusinessRuleException(
                    "A " + type + " must be placed under a " + type.requiredParent() + ", not a " + parent.getType());
        }
    }
}
