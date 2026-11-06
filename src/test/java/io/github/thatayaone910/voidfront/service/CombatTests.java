package io.github.thatayaone910.voidfront.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import io.github.thatayaone910.voidfront.sector.*;
import io.github.thatayaone910.voidfront.sector.hazard.Asteroid;
import io.github.thatayaone910.voidfront.sector.hazard.SpaceHazard;

import static org.junit.jupiter.api.Assertions.*;

public class CombatTests {
    private Sector sector;
    private CombatService service;
    private Starship testStarship1, testStarship2,testbot4, deadBot, bot5;
    private String testStarshipName1 = "TESTBOT", testStarshipName2= "TESTBOT2",
            testDeadStarship = "DEADBOT", testBot4 = "TESTBOT4", testBot5= "TESTBOT5";
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        // create a sector for eac test
        this.sector = new Sector(20, 20, 3, 3, 3);

        //create a movement service for cases
        this.service = new CombatService(sector);

        //create test callsign
        ObjectMapper mapper = new ObjectMapper();
        String deployJson = "{\"callsign\":\"TESTBOT\",\"command\":\"deploy\"," +
                "\"arguments\":[\"sniper\",1,5]}";
        JsonNode deployNode = mapper.readTree(deployJson);
        String callsign = deployNode.get("callsign").asText();
        JsonNode arguments = deployNode.get("arguments");
        this.testStarship1 = new Starship(callsign, arguments);
        this.testStarship1.setPosition(new Position(5, 5));
        this.sector.addStarship(this.testStarship1);

        String deployJson2 = "{\"callsign\":\"TESTBOT2\",\"command\":\"deploy\"," +
                "\"arguments\":[\"sniper\",1,5]}";
        JsonNode deployNode2 = mapper.readTree(deployJson2);
        String callsign2 = deployNode2.get("callsign").asText();
        JsonNode arguments2 = deployNode2.get("arguments");
        this.testStarship2 = new Starship(callsign2, arguments2);
        this.testStarship2.setPosition(new Position(5, 7));
        this.sector.addStarship(this.testStarship2);
        this.testStarship2.decreaseShots();
        this.testStarship2.decreaseShieldStrength();


        String deployJson3 = "{\"callsign\":\"DEADBOT\",\"command\":\"deploy\"," +
                "\"arguments\":[\"sniper\",0,0]}";
        JsonNode deployNode3 = mapper.readTree(deployJson3);
        String callsign3 = deployNode3.get("callsign").asText();
        JsonNode arguments3 = deployNode3.get("arguments");
        this.deadBot = new Starship(callsign3, arguments3);
        this.deadBot.setPosition(new Position(5, 3));
        this.sector.addStarship(this.deadBot);

        String deployJson4 = "{\"callsign\":\"TESTBOT4\",\"command\":\"deploy\"," +
                "\"arguments\":[\"sniper\",1,5]}";
        JsonNode deployNode4 = mapper.readTree(deployJson4);
        String callsign4 = deployNode4.get("callsign").asText();
        JsonNode arguments4 = deployNode4.get("arguments");
        this.testbot4 = new Starship(callsign4, arguments4);
        this.testbot4.setPosition(new Position(5, 10));
        this.sector.addStarship(this.testbot4);

        String deployJson5 = "{\"callsign\":\"TESTBOT5\",\"command\":\"deploy\"," +
                "\"arguments\":[\"sniper\",1,5]}";
        JsonNode deployNode5 = mapper.readTree(deployJson5);
        String callsign5 = deployNode5.get("callsign").asText();
        JsonNode arguments5 = deployNode5.get("arguments");
        this.bot5 = new Starship(callsign5, arguments5);
        this.bot5.setPosition(new Position(5, 2));
        this.sector.addStarship(this.bot5);

        this.sector.removeSpaceHazards();
    }

    @AfterEach
    public void tearDown() {
        // clear the sector after each test
        sector.clearSector();
    }

    /**Fire test cases*/
    @Test
    void fireStarshipDoesNotExistNull() throws JsonProcessingException {
        String fireNonexistent = this.service.fire("BUMBLEBEE");
        JsonNode response = this.mapper.readTree(fireNonexistent);

        assertNotNull(response);
        assertEquals("ERROR", response.get("result").asText());
        assertEquals("Starship not found", response.get("data").get("message").asText());
    }

    @Test
    void fireDecreaseShotsIfMiss() throws JsonProcessingException {
        String fire4 = this.service.fire(this.testBot4);
        JsonNode missed = this.mapper.readTree(fire4);

        assertEquals("OK", missed.get("result").asText());
        assertEquals("Miss", missed.get("data").get("message").asText());
        assertEquals(4, this.testbot4.getShots());
    }

    @Test
    void fireStarshipDecreaseTargetStarshipShields() throws JsonProcessingException {
        this.sector.removeStarship(this.deadBot);
        String fire2 = this.service.fire(this.testStarshipName2);
        JsonNode hit4 = this.mapper.readTree(fire2);

        assertEquals("OK", hit4.get("result").asText());
        assertEquals(this.testbot4.getCallsign(), hit4.get("data").get("callsign").asText());
        assertEquals(0,this.testbot4.getShieldStrength());
    }

    @Test
    void fireStarshipDoesNotFireIfZeroShots() throws JsonProcessingException {
        this.testStarship2.decreaseShots(); //decrease bot2 to 0 shots
        this.testStarship2.decreaseShots(); //decrease bot2 to 0 shots
        this.testStarship2.decreaseShots(); //decrease bot2 to 0 shots
        this.testStarship2.decreaseShots(); //decrease bot2 to 0 shots'

        String fire2= this.service.fire(this.testStarshipName2);
        JsonNode response = mapper.readTree(fire2);

        assertEquals("ERROR", response.get("result").asText());
    }

    @Test
    void fireStarshipDoesNotDamageItself() throws JsonProcessingException {
        String fire4 = service.fire(testBot4);
        JsonNode response = mapper.readTree(fire4);

        assertEquals(4,response.get("state").get("shots").asInt());
        assertEquals(1,this.testbot4.getShieldStrength());
    }

    @Test
    void fireStarshipHitsStarshipWithinRange() throws JsonProcessingException {
        String fire1=service.fire(testStarshipName2);
        JsonNode response = mapper.readTree(fire1);

        assertEquals("OK",response.get("result").asText());
        assertEquals("Hit", response.get("data").get("message").asText());
    }

    @Test
    void fireDoesNotFireOnDeadStarship(){
        String fire4 = this.service.fire(this.testStarshipName1);

        assertNotEquals(-1, deadBot.getShieldStrength(),
                "Must not decrease shields for dead callsigns");
    }

    @Test
    void fireStarshipMissesIfTargetNotWithinVisibility() throws JsonProcessingException {
        testStarship2.setPosition(new Position(5,9));
        String fire= service.fire(testStarshipName1);
        JsonNode response =mapper.readTree(fire);

        assertEquals("OK", response.get("result").asText());
        assertEquals("Miss",response.get("data").get("message").asText());
        assertEquals(4,testStarship1.getShots());
    }

    @Test
    void fireStarshipDoesNotFireBehindIt(){
        String fire1 = this.service.fire(testStarshipName1);

        assertEquals(1,this.bot5.getShieldStrength());
    }

    @Test
    void fireDoesNotFireItself() throws JsonProcessingException {
        String fire1 = service.fire(testStarshipName1);
        JsonNode hit2 = mapper.readTree(fire1);

        assertEquals(1,testStarship1.getShieldStrength());
    }

    @Test
    void fireKillsIfTargetShieldBelowZero() throws JsonProcessingException {
        String fire1 = service.fire(testBot5);
        JsonNode hit2 = mapper.readTree(fire1);

        assertEquals("Hit", hit2.get("data").get("message").asText());
        assertEquals("DEAD", hit2.get("data").get("state").get("status").asText());
        assertEquals(1, hit2.get("state").get("killCount").asInt());
    }

    @Test
    void fireDoesNotDecreaseShotsBelowZero() {
        this.bot5.decreaseShots();
        this.bot5.decreaseShots();
        this.bot5.decreaseShots();
        this.bot5.decreaseShots();
        this.bot5.decreaseShots();

        String fire5 = this.service.fire(this.testBot5);
        assertEquals(0,this.bot5.getShots(),"Shots must never be negative");
    }

    @Test
    void fireMissesIfHitsSpaceHazard() throws JsonProcessingException{
        sector.addSpaceHazard(new Asteroid(5,6));

        String fire= service.fire(testStarshipName1);
        JsonNode response = mapper.readTree(fire);

        assertEquals("OK", response.get("result").asText());
        assertEquals("Miss",response.get("data").get("message").asText());
        assertEquals(4,testStarship2.getShots());
    }

    @Test
    void fireDoesNotFireWhileReloading() throws JsonProcessingException{
        testStarship2.setStatus("REARMING");
        String fire= service.fire(testStarshipName2);
        JsonNode response = mapper.readTree(fire);

        assertEquals("ERROR", response.get("result").asText());
        assertEquals("Cannot fire while rearming or repairing",response.get("data").get("message").asText());
        assertEquals(4,testStarship2.getShots());
    }

    @Test
    void fireDoesNotFireWhileRepairing() throws JsonProcessingException{
        testStarship2.setStatus("REPAIRING");
        String fire= service.fire(testStarshipName2);
        JsonNode response = mapper.readTree(fire);

        assertEquals("ERROR", response.get("result").asText());
        assertEquals("Cannot fire while rearming or repairing",response.get("data").get("message").asText());
        assertEquals(4,testStarship2.getShots());
    }

    @Test
    void fireIgnoresDeadStarships() throws JsonProcessingException{
        deadBot.setStatus("DEAD");
        testbot4.setPosition(new Position(8,8));

        String fire = service.fire(testStarshipName1);
        JsonNode response = mapper.readTree(fire);

        assertEquals("OK", response.get("result").asText());
        assertEquals("Miss",response.get("data").get("message").asText());
        assertEquals(5,testbot4.getShots());
    }

    /**Reload test cases*/

    @Test
    void rearmStarshipDoesNotExist() throws JsonProcessingException {
        String rearmNonexistent = this.service.rearm("BUMBLEBEE");
        JsonNode response = this.mapper.readTree(rearmNonexistent);

        assertEquals("ERROR", response.get("result").asText());
        assertEquals("Starship not found",
                response.get("data").get("message").asText());
    }

    @Test
    void rearmsStarshipShotsToMax() throws JsonProcessingException {
        String rearm2 = this.service.rearm(this.testStarshipName2);
        JsonNode response = this.mapper.readTree(rearm2);

        assertEquals("OK",response.get("result").asText());
        assertEquals("Done",response.get("data").get("message").asText());
        assertEquals(5,this.testStarship2.getShots());
    }

    @Test
    void rearmStaysTheSameIfShotsIsMax() throws JsonProcessingException {
        String rearm1 = this.service.rearm(this.testStarshipName1);
        JsonNode response = this.mapper.readTree(rearm1);

        assertEquals("ERROR",response.get("result").asText());
        assertEquals("Shots at maximum capacity",
                response.get("data").get("message").asText());
        assertEquals(5,this.testStarship1.getShots());

    }

    @Test
    void rearmDoesNotWorkOnDeadStarship() throws JsonProcessingException {
        this.sector.removeStarship(this.deadBot);
        String rearm2 = this.service.rearm(this.testDeadStarship);
        JsonNode response = this.mapper.readTree(rearm2);

        assertEquals("ERROR", response.get("result").asText());
        assertEquals("Starship not found", response.get("data").get("message").asText());
    }

    @Test
    void rearmDoesnotWorkIfStarshipIsDead() throws JsonProcessingException{
        deadBot.setStatus("DEAD");

        String rearm = service.rearm(testDeadStarship);
        JsonNode response = mapper.readTree(rearm);

        assertEquals("ERROR", response.get("result").asText());
    }

    @Test
    void rearmResponseReturnsUpdatedShot() throws JsonProcessingException{
        String rearm = service.rearm(testStarshipName2);
        JsonNode response = mapper.readTree(rearm);

        assertEquals("OK",response.get("result").asText());
        assertEquals(5, response.get("state").get("shots").asInt());
        assertEquals(5, testStarship2.getShots());
    }


    /**Repair test cases*/
    @Test
    void repairStarshipDoesNotExist() throws JsonProcessingException {
        String repairNonexistent = this.service.repair("TESTBOT#");
        JsonNode repairRes = this.mapper.readTree(repairNonexistent);

        assertEquals("ERROR", repairRes.get("result").asText());
        assertEquals("Starship not found",
                repairRes.get("data").get("message").asText());

    }

    @Test
    void repairStarshipShieldsIfLow() throws JsonProcessingException {
        this.testStarship2.decreaseShots();

        String repair = this.service.repair(this.testStarshipName2);
        JsonNode response = this.mapper.readTree(repair);

        assertEquals(1,response.get("state").get("shields").asInt());
        assertEquals("OK",response.get("result").asText());
        assertEquals("Done",response.get("data").get("message").asText());
        assertEquals(1,response.get("state").get("shields").asInt());
    }

    @Test
    void repairStarshipStaysTheSameIfShieldsIsMax() throws JsonProcessingException {
        String repair1 = this.service.repair(this.testStarshipName1);
        JsonNode response = this.mapper.readTree(repair1);

        assertEquals(1,this.testStarship1.getShieldStrength());
        assertEquals("ERROR", response.get("result").asText());
        assertEquals("Shields at maximum capacity",response.get("data").get("message").asText());
    }

    @Test
    void repairDoesNotWorkIfRObotIsDead() throws JsonProcessingException{
        deadBot.setStatus("DEAD");

        String repairDead = service.repair(testDeadStarship);
        JsonNode response = mapper.readTree(repairDead);

        assertEquals("ERROR", response.get("result").asText());
        assertEquals("Starship not found", response.get("data").get("message").asText());
    }

    @Test
    void repairResponseReturnsUpdatedShieldAndNormalStatus() throws JsonProcessingException{
        String repair = service.repair(testStarshipName2);
        JsonNode response = mapper.readTree(repair);

        assertEquals("OK", response.get("result").asText());
        assertEquals("Done", response.get("data").get("message").asText());
        assertEquals("NORMAL", response.get("state").get("status").asText());
        assertEquals(1,response.get("state").get("shields").asInt());
    }

}
