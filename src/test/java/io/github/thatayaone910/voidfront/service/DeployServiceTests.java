package io.github.thatayaone910.voidfront.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;


import io.github.thatayaone910.voidfront.service.DeployService;
import io.github.thatayaone910.voidfront.sector.Sector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeployServiceTests {

    private Sector sector;
    private DeployService service;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        sector = new Sector(10, 10, 5, 3, 2);
        service = new DeployService(sector);
    }

    @AfterEach
    void tearDown() {
        sector.clearSector();
    }

    @Test
    void deployCommandReturnsOK() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals("OK", jsonResponse.get("result").asText());
    }

    @Test
    void deployReturnsNormalStatus() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals("NORMAL", jsonResponse.get("state").get("status").asText());
    }

    @Test
    void deploySameStarshipTwiceReturnsError() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        service.deployStarship("HAL", arguments);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        System.out.println("Second deploy response: " + json);
        assertEquals("ERROR", jsonResponse.get("result").asText());
    }

    @Test
    void duplicateCallsSignIsRejectedAfterOtherPlayersHaveJoined() throws Exception {
        JsonNode arguments = mapper.readTree("[\"sniper\",3,3]");
        service.deployStarship("ALPHA", arguments);
        service.deployStarship("BRAVO", arguments);

        JsonNode response = mapper.valueToTree(service.deployStarship("BRAVO", arguments));

        assertEquals("ERROR", response.get("result").asText());
    }

    @Test
    void malformedDeploymentArgumentsReturnAnError() throws Exception {
        JsonNode response = mapper.valueToTree(service.deployStarship("ALPHA", mapper.readTree("[\"scout\"]")));

        assertEquals("ERROR", response.get("result").asText());
        assertTrue(sector.getAllStarships().isEmpty());
    }

    @Test
    void unsupportedShipClassIsRejected() throws Exception {
        JsonNode response = mapper.valueToTree(
                service.deployStarship("ALPHA", mapper.readTree("[\"scout\",1,5]")));

        assertEquals("ERROR", response.get("result").asText());
        assertTrue(sector.getAllStarships().isEmpty());
    }

    @Test
    void deployReturnsPosition() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertNotNull(jsonResponse.get("data").get("position"));
    }

    @Test
    void deploysShipsAtDistinctUnoccupiedPositions() throws Exception {
        JsonNode arguments = mapper.readTree("[\"sniper\",3,3]");
        sector.addSpaceHazard(new io.github.thatayaone910.voidfront.sector.hazard.Asteroid(0, 0));

        for (int i = 0; i < 12; i++) {
            service.deployStarship("SHIP-" + i, arguments);
        }

        List<String> positions = sector.getAllStarships().stream()
                .map(starship -> starship.getPosition().getX() + "," + starship.getPosition().getY())
                .toList();
        assertEquals(12, positions.stream().distinct().count());
        assertFalse(positions.contains("0,0"));
    }
    @Test
    void deployReturnsNorthHeading() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals("NORTH", jsonResponse.get("state").get("heading").asText());
    }
    @Test
    void deployReturnsVisibility() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertNotNull(jsonResponse.get("data").get("visibility"));
    }

    @Test
    void deployReturnsCorrectShields() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals(3, jsonResponse.get("state").get("shields").asInt());  // Changed from 3 to 1
    }

    @Test
    void deployReturnsCorrectShots() throws Exception {
        String args = "[\"sniper\",3,3]";
        JsonNode arguments = mapper.readTree(args);
        Object response = service.deployStarship("HAL", arguments);
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals(3, jsonResponse.get("state").get("shots").asInt());  // Changed from 3 to 5
    }




}