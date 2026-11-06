package io.github.thatayaone910.voidfront.server;

import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;
import java.util.InvalidPropertiesFormatException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    @Test
    void testNegativePortMustThrowException(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Server(-1, "sector.properties")
        );
    }

    @Test
    void testSystemReservedPortMustThrowException(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Server(111, "sector.properties")
        );
    }

    @Test
    void testNullConfigPathMustThrowException(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Server(2026, null)
        );
    }

    @Test
    void testEmptyConfigPathMustThrowException(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Server(2026, "")
        );
    }

    @Test
    void testInvalidConfigExtensionMustThrowException(){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Server(2026, "config.json")
        );
    }

    @Test
    void testCreatingServerObject(){
        assertDoesNotThrow(() -> {
            new Server(1234, "sector.properties");
        });
    }

    @Test
    void testPortNumberIsNotNull(){
        assertNotEquals(0,
                new Server(1234, "sector.properties").getPort());
    }

    @Test
    void testCorrectPortNumberUsed(){
        assertEquals(1234,
                new Server(1234, "sector.properties").getPort());
    }

    @Test
    void testSectorIsNotNull(){
        assertNotNull(new Server(1234, "sector.properties").getSector());
    }

    @Test
    void testSectorSettingsLoadFromClasspathResource() {
        Server server = new Server(1234, "sector.properties");

        assertEquals(30, server.getSector().getRight() + 1);
        assertEquals(30, server.getSector().getTop() + 1);
        assertEquals(30, server.getSector().getScanRange());
        assertEquals(5, server.getSector().getRearmDuration());
    }
}
