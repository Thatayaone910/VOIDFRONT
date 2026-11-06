package io.github.thatayaone910.voidfront.sector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import io.github.thatayaone910.voidfront.sector.hazard.Nebula;
import io.github.thatayaone910.voidfront.sector.hazard.Asteroid;
import io.github.thatayaone910.voidfront.sector.hazard.SpaceHazard;
import io.github.thatayaone910.voidfront.sector.hazard.VoidRift;

class SpaceHazardTest {

    @Test
    void testNebulaPosition() {
        SpaceHazard lake = new Nebula(10, 20);
        assertEquals(10, lake.getPosition().getX());
        assertEquals(20, lake.getPosition().getY());
        assertEquals("Nebula", lake.getType());
    }

    @Test
    void testAsteroidPosition() {
        SpaceHazard mountain = new Asteroid(5, 5);
        assertEquals(5, mountain.getPosition().getX());
        assertEquals(5, mountain.getPosition().getY());
        assertEquals("Asteroid", mountain.getType());
    }

    @Test
    void testVoidRiftPosition() {
        SpaceHazard pit = new VoidRift(0, 0);
        assertEquals(0, pit.getPosition().getX());
        assertEquals("VoidRift", pit.getType());
    }

    @Test
    void testBlocksPositionSuccess() {
        SpaceHazard lake = new Nebula(10, 10);
        Position pos = new Position(10, 10);

        assertTrue(lake.blocksPosition(pos), "hazard should block its own position");
    }

    @Test
    void testBlocksPositionFailure() {
        SpaceHazard mountain = new Asteroid(10, 10);
        Position differentPos = new Position(11, 11);

        assertFalse(mountain.blocksPosition(differentPos), "hazard should not block a different position");
    }
}
