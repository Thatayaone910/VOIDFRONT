package io.github.thatayaone910.voidfront.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import io.github.thatayaone910.voidfront.sector.*;

import static org.junit.jupiter.api.Assertions.*;

public class QuitServiceTests {

    private Sector sector;
    private QuitService quitService;
    private Starship testStarship;
    private ObjectMapper mapper = new ObjectMapper();

    private static final String STARSHIP_NAME = "HAL";

    @BeforeEach
    void setUp() {
        // Create a fresh sector for each test
        sector = new Sector(10, 10, 5, 3, 2);
        quitService = new QuitService(sector);

        // Create and deploy a test callsign
        String deployArgs = "[\"sniper\",3,3]";
        try {
            JsonNode arguments = mapper.readTree(deployArgs);
            testStarship = new Starship(STARSHIP_NAME, arguments);
            testStarship.setPosition(new Position(5, 5));
            sector.addStarship(testStarship);
        } catch (Exception e) {
            fail("Failed to set up test callsign: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        sector.clearSector();
    }



    @Test
    void quitReturnsFinalHeading() {
        // Act
        Object response = quitService.getQuit(STARSHIP_NAME);

        // Assert
        assertNotNull(response);

        try {
            String json = mapper.writeValueAsString(response);
            JsonNode jsonResponse = mapper.readTree(json);
            assertNotNull(jsonResponse.get("state").get("heading"));
        } catch (Exception e) {
            fail("Response should include heading in state");
        }
    }    @Test
    void quitDoesNotAffectOtherStarships() throws Exception {
        // Arrange - create a second callsign
        String secondStarshipName = "SECOND_STARSHIP";
        String deployArgs = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(deployArgs);
        Starship secondStarship = new Starship(secondStarshipName, arguments);
        secondStarship.setPosition(new Position(7, 7));
        sector.addStarship(secondStarship);

        // Act - quit first callsign
        quitService.getQuit(STARSHIP_NAME);

        // Assert - second callsign still exists and is NORMAL
        assertNotNull(sector.findStarship(secondStarshipName));
        Starship foundStarship = sector.findStarship(secondStarshipName);
        assertEquals("NORMAL", foundStarship.getStatus());
    }
}