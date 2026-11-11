package lk.smartcity360.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import lk.smartcity360.pattern.flyweight.LightType;
import lk.smartcity360.pattern.flyweight.LightTypeFactory;
import lk.smartcity360.pattern.flyweight.StreetLightContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlyweightTest {

    @BeforeEach
    void emptyPool() {
        LightTypeFactory.reset();
    }

    @Test
    void identicalTypesAreShared() {
        LightType a = LightTypeFactory.get("LED", 60, "white");
        LightType b = LightTypeFactory.get("LED", 60, "white");

        assertSame(a, b);
        assertEquals(1, LightTypeFactory.cachedTypes());
    }

    @Test
    void differentTypesGetTheirOwnInstance() {
        LightType a = LightTypeFactory.get("LED", 60, "white");
        LightType b = LightTypeFactory.get("LED", 45, "amber");

        assertNotSame(a, b);
        assertEquals(2, LightTypeFactory.cachedTypes());
    }

    @Test
    void contextsKeepTheirOwnStateWhileSharingTheType() {
        LightType type = LightTypeFactory.get("LED", 60, "white");
        StreetLightContext main = new StreetLightContext("Main St", type, true);
        StreetLightContext lake = new StreetLightContext("Lake Rd", type, false);

        assertEquals("LED 60W (white) at Main St is ON", main.display());
        assertEquals("LED 60W (white) at Lake Rd is OFF", lake.display());

        lake.setOn(true);
        assertEquals("LED 60W (white) at Lake Rd is ON", lake.display());
        assertEquals("LED 60W (white) at Main St is ON", main.display());
        assertSame(main.getType(), lake.getType());
    }

    @Test
    void dimmingProfilesAndHexObjectIdsAreConsistent() {
        LightType t120 = LightTypeFactory.get("SL-LED-120", 120, "Cool White");
        LightType t60 = LightTypeFactory.get("SL-LED-60", 60, "Warm White");

        // Hour 20 brightness per profile
        assertEquals(100, t120.getBrightness(20));
        assertEquals(80, t60.getBrightness(20));

        StreetLightContext okLight = new StreetLightContext("GR-001", "6.9147, 79.8489", t120, false);
        StreetLightContext faultyLight = new StreetLightContext("MAIN-00000", "6.85, 79.84", t120, true);

        assertEquals("OK", okLight.getState());
        assertEquals(100, okLight.getBrightness(20));
        assertEquals("100%", okLight.getBrightnessPercent(20));

        assertEquals("FAULTY", faultyLight.getState());
        assertEquals(0, faultyLight.getBrightness(20));
        assertEquals("0%", faultyLight.getBrightnessPercent(20));

        // Shared object reference identity
        assertSame(okLight.getType(), faultyLight.getType());
        assertEquals(okLight.getType().getHexId(), faultyLight.getType().getHexId());

        // Test urban highway and community specifications
        LightType highway = LightTypeFactory.get("URBAN-HIGHWAY-150", 150, "Cool White 5700K");
        LightType community = LightTypeFactory.get("SMART-COMMUNITY-70", 70, "Warm Amber 3000K");
        assertEquals(100, highway.getBrightness(20));
        assertEquals(80, community.getBrightness(20));
        assertNotSame(highway, community);
    }
}
