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

    @Test
    void usageAddsUpAtEveryLevel() {
        ZoneGroup city = new ZoneGroup("Colombo");
        ZoneGroup district = new ZoneGroup("Colombo 03");
        ZoneGroup street = new ZoneGroup("Galle Road");

        Building mall = new Building("Shopping Mall", 450.00, true);
        Building hospital = new Building("City Hospital", 380.50, true);

        street.add(mall);
        street.add(hospital);
        district.add(street);
        city.add(district);

        assertEquals(450.00, mall.getUsage(), 0.001);
        assertEquals(380.50, hospital.getUsage(), 0.001);
        assertEquals(830.50, street.getUsage(), 0.001);
        assertEquals(830.50, district.getUsage(), 0.001);
        assertEquals(830.50, city.getUsage(), 0.001);
    }

    @Test
    void powerSwitchCascadesAcrossHierarchy() {
        ZoneGroup city = new ZoneGroup("Colombo");
        Building mall = new Building("Shopping Mall", 450.00, true);
        Building hospital = new Building("City Hospital", 380.50, true);
        city.add(mall);
        city.add(hospital);

        assertTrue(city.isPowerOn());
        assertTrue(mall.isPowerOn());
        assertTrue(hospital.isPowerOn());

        city.setPower(false);

        org.junit.jupiter.api.Assertions.assertFalse(city.isPowerOn());
        org.junit.jupiter.api.Assertions.assertFalse(mall.isPowerOn());
        org.junit.jupiter.api.Assertions.assertFalse(hospital.isPowerOn());

        city.setPower(true);
        assertTrue(city.isPowerOn());
        assertTrue(mall.isPowerOn());
        assertTrue(hospital.isPowerOn());
    }

    private static Zone zone(Long id, String name, ZoneType type, Long parent) throws Exception {
        Zone zone = new Zone(name, type, parent);
        Field field = Zone.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(zone, id);
        return zone;
    }
}
