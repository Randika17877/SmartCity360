package lk.smartcity360.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import lk.smartcity360.entity.Zone;
import lk.smartcity360.entity.ZoneType;
import lk.smartcity360.pattern.composite.Building;
import lk.smartcity360.pattern.composite.CityComponent;
import lk.smartcity360.pattern.composite.ZoneGroup;
import lk.smartcity360.pattern.composite.ZoneTreeBuilder;
import org.junit.jupiter.api.Test;

class CompositeTest {

    @Test
    void leafExecutesOnItself() {
        assertEquals(List.of("DIM executed at building Hall"), new Building("Hall").execute("DIM"));
    }

    @Test
    void groupForwardsToEveryDescendant() {
        ZoneGroup city = new ZoneGroup("City");
        ZoneGroup street = new ZoneGroup("Street");
        city.add(street);
        street.add(new Building("A"));
        street.add(new Building("B"));

        List<String> log = city.execute("OFF");

        assertEquals(4, log.size());
        assertTrue(log.get(0).contains("zone City"));
        assertTrue(log.get(3).contains("building B"));
    }

    @Test
    void removedChildIsNoLongerReached() {
        ZoneGroup group = new ZoneGroup("G");
        Building building = new Building("B");
        group.add(building);
        group.remove(building);

        assertEquals(1, group.execute("X").size());
    }

    @Test
    void persistedRowsBecomeATree() throws Exception {
        List<Zone> rows = List.of(
                zone(1L, "Metro", ZoneType.CITY, null),
                zone(2L, "Central", ZoneType.DISTRICT, 1L),
                zone(3L, "Main St", ZoneType.STREET, 2L),
                zone(4L, "Town Hall", ZoneType.BUILDING, 3L),
                zone(5L, "Harbour", ZoneType.DISTRICT, 1L));

        List<CityComponent> roots = ZoneTreeBuilder.roots(rows);

        assertEquals(1, roots.size());
        assertEquals("Metro", roots.get(0).getName());
        // Metro, Central, Main St, Town Hall, Harbour
        assertEquals(5, roots.get(0).execute("PING").size());
    }

    @Test
    void zoneWithMissingParentBecomesARoot() throws Exception {
        List<Zone> rows = List.of(zone(7L, "Orphan", ZoneType.DISTRICT, 99L));

        assertEquals(1, ZoneTreeBuilder.roots(rows).size());
    }

    @Test
    void hierarchyRulesAreEncodedInTheType() {
        assertTrue(ZoneType.CITY.isRoot());
        assertEquals(ZoneType.CITY, ZoneType.DISTRICT.requiredParent());
        assertEquals(ZoneType.DISTRICT, ZoneType.STREET.requiredParent());
        assertEquals(ZoneType.STREET, ZoneType.BUILDING.requiredParent());
    }

    private static Zone zone(Long id, String name, ZoneType type, Long parent) throws Exception {
        Zone zone = new Zone(name, type, parent);
        Field field = Zone.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(zone, id);
        return zone;
    }
}
