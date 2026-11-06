package io.github.thatayaone910.voidfront.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import io.github.thatayaone910.voidfront.service.*;
import io.github.thatayaone910.voidfront.sector.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClientHandlerTest {

        private Sector sector;
        private DeployService deployService;
        private QueryService queryService;
        private CombatService combatService;
        private QuitService quitService;
        private ObjectMapper mapper;

        @BeforeEach
        void setUp() {
                sector = new Sector(10, 10, 5, 3, 2);
                deployService = new DeployService(sector);
                queryService = new QueryService(sector);
                combatService = new CombatService(sector);
                quitService = new QuitService(sector);
                mapper = new ObjectMapper();
        }

        @AfterEach
        void tearDown() {
                sector.clearSector();
        }

        @Test
        void deployCommandReturnsOK() throws Exception {
                // Act
                Object response = deployService.deployStarship("HAL", mapper.readTree("[\"sniper\",3,3]"));
                String json = mapper.writeValueAsString(response);
                JsonNode jsonResponse = mapper.readTree(json);

                // Assert
                assertEquals("OK", jsonResponse.get("result").asText());
        }

        @Test
        void scanCommandReturnsOK() throws Exception {
                // Arrange - deploy first
                deployService.deployStarship("HAL", mapper.readTree("[\"sniper\",3,3]"));

                // Act
                Object response = queryService.getScan("HAL");
                String json = mapper.writeValueAsString(response);
                JsonNode jsonResponse = mapper.readTree(json);

                // Assert
                assertEquals("OK", jsonResponse.get("result").asText());
                assertNotNull(jsonResponse.get("data").get("objects"));
        }

        @Test
        void stateCommandReturnsOK() throws Exception {
                // Arrange
                deployService.deployStarship("HAL", mapper.readTree("[\"sniper\",3,3]"));

                // Act
                Object response = queryService.getState("HAL");
                String json = mapper.writeValueAsString(response);
                JsonNode jsonResponse = mapper.readTree(json);

                // Assert
                assertEquals("OK", jsonResponse.get("result").asText());
                assertNotNull(jsonResponse.get("state"));
        }

        @Test
        void headingCommandReturnsOK() throws Exception {
                // Arrange
                deployService.deployStarship("HAL", mapper.readTree("[\"sniper\",3,3]"));

                // Act
                Object response = queryService.getHeading("HAL");
                String json = mapper.writeValueAsString(response);
                JsonNode jsonResponse = mapper.readTree(json);

                // Assert
                assertEquals("OK", jsonResponse.get("result").asText());
                assertNotNull(jsonResponse.get("heading"));
        }

        @Test
        void quitCommandReturnsDead() throws Exception {
                // Arrange
                deployService.deployStarship("HAL", mapper.readTree("[\"sniper\",3,3]"));

                // Act
                Object response = quitService.getQuit("HAL");
                String json = mapper.writeValueAsString(response);
                JsonNode jsonResponse = mapper.readTree(json);

                // Assert
                assertEquals("OK", jsonResponse.get("result").asText());
                assertEquals("DEAD", jsonResponse.get("state").get("status").asText());
        }

        @Test
        void unknownStarshipReturnsError() throws Exception {
                // Act
                Object response = queryService.getState("NONEXISTENT");
                String json = mapper.writeValueAsString(response);
                JsonNode jsonResponse = mapper.readTree(json);

                // Assert
                assertEquals("ERROR", jsonResponse.get("result").asText());
        }

}