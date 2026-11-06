package io.github.thatayaone910.voidfront.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thatayaone910.voidfront.sector.Heading;
import org.junit.jupiter.api.*;
import io.github.thatayaone910.voidfront.sector.Starship;
import io.github.thatayaone910.voidfront.sector.Sector;
import io.github.thatayaone910.voidfront.sector.Position;
import io.github.thatayaone910.voidfront.sector.hazard.Asteroid;

import static org.junit.jupiter.api.Assertions.*;

public class QueryServiceTests {

    private Sector sector;
    private QueryService service;
    private Starship testStarship;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        sector = new Sector(10, 10, 5, 3, 2);
        service = new QueryService(sector);
        String deployJson = "{\"callsign\":\"HAL\",\"command\":\"deploy\"," +
                "\"arguments\":[\"sniper\",3,3]}";
        JsonNode deployNode = mapper.readTree(deployJson);
        testStarship = new Starship("HAL", deployNode.get("arguments"));
        sector.addStarship(testStarship);
    }

    @AfterEach
    void tearDown() {
        sector.clearSector();
    }

    @Test
    void stateCommandReturnsOK() throws Exception {
        Object response = service.getState("HAL");
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertNotNull(jsonResponse.get("state"));
    }

    @Test
    void headingCommandReturnsOK() throws Exception {
        Object response = service.getHeading("HAL");
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertNotNull(jsonResponse.get("heading"));
    }

    @Test
    void stateCommandReturnsNormalStatus() throws Exception {
        Object response = service.getState("HAL");
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals("NORMAL", jsonResponse.get("state").get("status").asText());
    }

    @Test
    void headingAfterTurnLeft() throws Exception {
        testStarship.rotateTo(Heading.WEST);
        Object response = service.getHeading("HAL");
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals("WEST", jsonResponse.get("heading").asText());
    }

    @Test
    void headingAfterTurnRight() throws Exception {
        testStarship.rotateTo(Heading.EAST);
        Object response = service.getHeading("HAL");
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals("EAST", jsonResponse.get("heading").asText());
    }

    @Test
    void scanReportsNorthAsIncreasingY() throws Exception {
        testStarship.setPosition(new Position(4, 4));
        Starship northStarship = new Starship("NORTH", mapper.readTree("[\"sniper\",1,5]"));
        northStarship.setPosition(new Position(4, 6));
        sector.addStarship(northStarship);

        JsonNode response = mapper.valueToTree(service.getScan("HAL"));
        JsonNode visibleStarship = null;
        for (JsonNode object : response.get("data").get("objects")) {
            if ("NORTH".equals(object.get("name").asText())) {
                visibleStarship = object;
                break;
            }
        }

        assertNotNull(visibleStarship);
        assertEquals("NORTH", visibleStarship.get("heading").asText());
        assertEquals(2, visibleStarship.get("distance").asInt());
    }

    @Test
    void scanReportsSpaceHazardsWithHazardDomainType() throws Exception {
        testStarship.setPosition(new Position(4, 4));
        sector.addSpaceHazard(new Asteroid(4, 6));

        JsonNode response = mapper.valueToTree(service.getScan("HAL"));
        JsonNode hazard = null;
        for (JsonNode object : response.get("data").get("objects")) {
            if ("Asteroid".equals(object.get("name").asText())) {
                hazard = object;
                break;
            }
        }

        assertNotNull(hazard);
        assertEquals("HAZARD", hazard.get("type").asText());
    }

    @Test
    void stateCommandForUnknownStarshipReturnsError() throws Exception {
        Object response = service.getState("UNKNOWN");
        String json = mapper.writeValueAsString(response);
        JsonNode jsonResponse = mapper.readTree(json);
        assertEquals("ERROR", jsonResponse.get("result").asText());
    }
}