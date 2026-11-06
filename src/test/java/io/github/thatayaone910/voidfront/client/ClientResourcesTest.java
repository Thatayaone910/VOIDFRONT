package io.github.thatayaone910.voidfront.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientResourcesTest {

    @Test
    void fxmlConfigurationAndSpritesAreAvailableOnClasspath() {
        String[] resources = {
                "/Voidfront.fxml",
                "/sector.properties",
                "/icons/entities/starship.png",
                "/icons/hazards/asteroid.png",
                "/icons/hazards/nebula.png",
                "/icons/hazards/void-rift.png",
                "/icons/effects/blast.png",
                "/icons/ui/shield.png",
                "/icons/ui/shots.png",
                "/icons/ui/time.png"
        };

        for (String resource : resources) {
            assertNotNull(getClass().getResource(resource), resource + " must be on the classpath");
        }
    }
}
