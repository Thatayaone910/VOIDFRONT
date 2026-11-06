package io.github.thatayaone910.voidfront.sector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipClassTest {

    @Test
    void exposesConfiguredClassesWithoutUiTypes() {
        assertEquals(java.util.List.of("sniper"), ShipClass.getShipClasses());
        assertEquals(java.util.List.of("sniper", 1, 5), ShipClass.getShipClass("SNIPER"));
    }

    @Test
    void rejectsUnknownOrMissingShipClasses() {
        assertThrows(IllegalArgumentException.class, () -> ShipClass.getShipClass("unknown"));
        assertThrows(IllegalArgumentException.class, () -> ShipClass.getShipClass(null));
    }
}
