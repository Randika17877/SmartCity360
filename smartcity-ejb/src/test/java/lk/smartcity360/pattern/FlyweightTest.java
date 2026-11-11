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
}
