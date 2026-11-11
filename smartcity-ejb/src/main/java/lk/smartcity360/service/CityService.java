package lk.smartcity360.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lk.smartcity360.entity.Zone;
import lk.smartcity360.entity.ZoneType;
import lk.smartcity360.pattern.composite.CityComponent;
import lk.smartcity360.pattern.composite.ZoneTreeBuilder;

/** Manages the city hierarchy and composite execution tree (Composite pattern). */
@Stateless
public class CityService {

    @PersistenceContext(unitName = "SmartCityPU")
    private EntityManager em;

    public Zone saveZone(Zone zone) {
        validateParent(zone);
        if (zone.getId() == null) {
            em.persist(zone);
            return zone;
        }
        return em.merge(zone);
    }

    public List<Zone> zones() {
        List<Zone> list = em.createQuery("SELECT z FROM Zone z ORDER BY z.id", Zone.class).getResultList();
        if (list.isEmpty()) {
            resetCity();
            list = em.createQuery("SELECT z FROM Zone z ORDER BY z.id", Zone.class).getResultList();
        }
        return list;
    }

    public long zoneCount() {
        return em.createQuery("SELECT COUNT(z) FROM Zone z", Long.class).getSingleResult();
    }

    public List<Zone> streets() {
        return em.createQuery("SELECT z FROM Zone z WHERE z.type = :st ORDER BY z.name", Zone.class)
                .setParameter("st", ZoneType.STREET)
                .getResultList();
    }

    public List<Zone> allSwitchableZones() {
        return zones();
    }

    /**
     * Adds a new building under a street and recalculates usage.
     */
    public Zone addBuilding(Long streetId, String buildingName, double usageKwh) {
        if (buildingName == null || buildingName.trim().isEmpty()) {
            throw new BusinessRuleException("Building name is required");
        }
        if (streetId == null) {
            throw new BusinessRuleException("Please select a street");
        }
        Zone street = em.find(Zone.class, streetId);
        if (street == null || street.getType() != ZoneType.STREET) {
            throw new BusinessRuleException("Selected parent must be a valid Street");
        }
        Zone building = new Zone(buildingName.trim(), ZoneType.BUILDING, streetId, Math.max(0.0, usageKwh), true);
        em.persist(building);
        return building;
    }

    /**
     * Switches power on or off for a zone and cascades recursively to all descendant nodes.
     */
    public void setPower(Long zoneId, boolean on) {
        if (zoneId == null) {
            throw new BusinessRuleException("Please select a city, district, street, or building");
        }
        Zone target = em.find(Zone.class, zoneId);
        if (target == null) {
            throw new BusinessRuleException("Zone with ID " + zoneId + " does not exist");
        }

        List<Zone> all = zones();
        Set<Long> affectedIds = new HashSet<>();
        affectedIds.add(zoneId);
        collectDescendantIds(zoneId, all, affectedIds);

        for (Zone z : all) {
            if (affectedIds.contains(z.getId())) {
                z.setPowerOn(on);
                em.merge(z);
            }
        }
    }

    private void collectDescendantIds(Long parentId, List<Zone> all, Set<Long> result) {
        for (Zone z : all) {
            if (parentId.equals(z.getParentId())) {
                result.add(z.getId());
                collectDescendantIds(z.getId(), all, result);
            }
        }
    }

    /**
     * Rebuilds the Composite tree from database rows, evaluates aggregated usage at each level,
     * and produces a flattened pre-order list of tree view items.
     */
    public List<ZoneTreeItem> getTreeItems() {
        List<Zone> all = zones();
        Map<Long, CityComponent> componentMap = ZoneTreeBuilder.index(all);
        Map<Long, List<Zone>> childrenByParent = new LinkedHashMap<>();
        List<Zone> roots = new ArrayList<>();

        for (Zone z : all) {
            if (z.getParentId() == null) {
                roots.add(z);
            } else {
                childrenByParent.computeIfAbsent(z.getParentId(), k -> new ArrayList<>()).add(z);
            }
        }

        List<ZoneTreeItem> items = new ArrayList<>();
        for (Zone root : roots) {
            buildTreeItemsRecursively(root, 0, childrenByParent, componentMap, items);
        }
        return items;
    }

    private void buildTreeItemsRecursively(Zone zone, int depth, Map<Long, List<Zone>> childrenByParent,
                                          Map<Long, CityComponent> componentMap, List<ZoneTreeItem> items) {
        CityComponent comp = componentMap.get(zone.getId());
        double usage = comp != null ? comp.getUsage() : (zone.getUsageKwh() != null ? zone.getUsageKwh() : 0.0);
        boolean power = comp != null ? comp.isPowerOn() : zone.isPowerOn();

        List<Zone> children = childrenByParent.get(zone.getId());
        boolean hasChildren = children != null && !children.isEmpty();

        items.add(new ZoneTreeItem(zone.getId(), zone.getParentId(), zone.getName(), zone.getType(), depth, usage, power, hasChildren));

        if (children != null) {
            for (Zone child : children) {
                buildTreeItemsRecursively(child, depth + 1, childrenByParent, componentMap, items);
            }
        }
    }

    /**
     * Calculates total city usage across all root nodes.
     */
    public double getTotalUsage() {
        List<Zone> all = zones();
        List<CityComponent> roots = ZoneTreeBuilder.roots(all);
        double total = 0.0;
        for (CityComponent root : roots) {
            total += root.getUsage();
        }
        return total;
    }

    /**
     * Resets the city tree to the default sample configuration matching the assessment scenario.
     */
    public void resetCity() {
        em.createQuery("DELETE FROM Zone").executeUpdate();

        // 1. Root City
        Zone colombo = new Zone("Colombo", ZoneType.CITY, null, 0.0, true);
        em.persist(colombo);
        em.flush();

        // 2. Districts
        Zone kollupitiya = new Zone("Colombo 03 - Kollupitiya", ZoneType.DISTRICT, colombo.getId(), 0.0, true);
        Zone cinnamonGardens = new Zone("Colombo 07 - Cinnamon Gardens", ZoneType.DISTRICT, colombo.getId(), 0.0, true);
        em.persist(kollupitiya);
        em.persist(cinnamonGardens);
        em.flush();

        // 3. Streets under Kollupitiya
        Zone galleRoad = new Zone("Galle Road", ZoneType.STREET, kollupitiya.getId(), 0.0, true);
        Zone dharmapala = new Zone("Dharmapala Mawatha", ZoneType.STREET, kollupitiya.getId(), 0.0, true);
        em.persist(galleRoad);
        em.persist(dharmapala);

        // 4. Street under Cinnamon Gardens
        Zone wardPlace = new Zone("Ward Place", ZoneType.STREET, cinnamonGardens.getId(), 0.0, true);
        em.persist(wardPlace);
        em.flush();

        // 5. Buildings under Galle Road
        em.persist(new Zone("Shopping Mall", ZoneType.BUILDING, galleRoad.getId(), 450.00, true));
        em.persist(new Zone("City Hospital", ZoneType.BUILDING, galleRoad.getId(), 380.50, true));

        // 6. Buildings under Dharmapala Mawatha
        em.persist(new Zone("Apartment Block A", ZoneType.BUILDING, dharmapala.getId(), 120.00, true));
        em.persist(new Zone("Supermarket", ZoneType.BUILDING, dharmapala.getId(), 95.25, true));

        // 7. Buildings under Ward Place
        em.persist(new Zone("Public Library", ZoneType.BUILDING, wardPlace.getId(), 150.00, true));
        em.persist(new Zone("Town Hall", ZoneType.BUILDING, wardPlace.getId(), 210.00, true));
        em.flush();
    }

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
