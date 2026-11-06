package io.github.thatayaone910.voidfront.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import io.github.thatayaone910.voidfront.sector.*;
import io.github.thatayaone910.voidfront.sector.hazard.Asteroid;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MovementTests {
    private Sector sector;
    private NavigationService service;
    private Starship testStarship, testStarship2;
    private String testStarshipName = "TESTBOT", testStarshipName2= "TESTBOT2";

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        // create a sector for eac test
        this.sector = new Sector(20, 20, 3, 3, 3);

        //create a movement service for cases
        this.service = new NavigationService(sector);

        //create test callsign
        ObjectMapper mapper = new ObjectMapper();
        String deployJson = "{\"callsign\":\"TESTBOT\",\"command\":\"deploy\",\"arguments\":[\"sniper\",2,5]}";
        JsonNode deployNode = mapper.readTree(deployJson);
        String callsign = deployNode.get("callsign").asText();
        JsonNode arguments = deployNode.get("arguments");
        this.testStarship = new Starship(callsign, arguments);
        this.testStarship.setPosition(new Position(5, 5));
        this.sector.addStarship(this.testStarship);

        String deployJson2 = "{\"callsign\":\"TESTBOT2\",\"command\":\"deploy\",\"arguments\":[\"sniper\",2,5]}";
        JsonNode deployNode2 = mapper.readTree(deployJson2);
        String callsign2 = deployNode2.get("callsign").asText();
        JsonNode arguments2 = deployNode2.get("arguments");
        this.testStarship2 = new Starship(callsign2, arguments2);
        this.testStarship2.setPosition(new Position(15, 15));
        this.sector.addStarship(this.testStarship2);

        this.sector.removeSpaceHazards();
    }

    @AfterEach
    public void tearDown() {
        // clear the sector after each test
        sector.clearSector();
    }

    //**Edge Cases*/
    @Test
    void getTurnStarshipDoesNotExistNull() {
        String testStarshipName = "Bumblebee";

        NavigationService.MovementResponse response = this.service.getTurn(testStarshipName, "left");
        assertNull(response);
    }


    @Test
    void moveStarshipDoesNotExistNull() {
        String testStarshipName = "Dino";

        NavigationService.MovementResponse response = this.service.move(testStarshipName, 7, "thrust");
        assertNull(response);

    }

    /**
     * Turn test cases
     */

    @Test
    void getTurnLeftFacingNorthToWest() {
        NavigationService.MovementResponse leftNorth = this.service.getTurn(this.testStarshipName, "left");

        assertNotNull(leftNorth);
        assertEquals("OK", leftNorth.result());
        assertEquals("Done", leftNorth.data().message());
        assertEquals("WEST", leftNorth.state().heading());
    }

    @Test
    void getTurnLeftFacingSouthToEast() {
        this.testStarship.setHeading(Heading.SOUTH);
        NavigationService.MovementResponse leftSouth = this.service.getTurn(this.testStarshipName, "left");

        assertNotNull(leftSouth);
        assertEquals("OK", leftSouth.result());
        assertEquals("Done", leftSouth.data().message());
        assertEquals("EAST", leftSouth.state().heading());
    }

    @Test
    void getTurnLeftFacingWestToSouth() {
        this.testStarship.setHeading(Heading.WEST);

        NavigationService.MovementResponse leftWest = this.service.getTurn(this.testStarshipName, "left");

        assertNotNull(leftWest);
        assertEquals("OK", leftWest.result());
        assertEquals("Done", leftWest.data().message());
        assertEquals("SOUTH", leftWest.state().heading());
    }

    @Test
    void getTurnLeftFacingEastToNorth() {
        this.testStarship.setHeading(Heading.EAST);

        NavigationService.MovementResponse leftEast = this.service.getTurn(this.testStarshipName, "left");

        assertNotNull(leftEast);
        assertEquals("OK", leftEast.result());
        assertEquals("Done", leftEast.data().message());
        assertEquals("NORTH", leftEast.state().heading());
    }

    @Test
    void getTurnRightFacingNorthToEast() {
        NavigationService.MovementResponse rightNorth = this.service.getTurn(this.testStarshipName, "right");

        assertNotNull(rightNorth);
        assertEquals("OK", rightNorth.result());
        assertEquals("Done", rightNorth.data().message());
        assertEquals("EAST", rightNorth.state().heading());
    }

    @Test
    void getTurnRightFacingEastToSouth() {
        this.testStarship.setHeading(Heading.EAST);

        NavigationService.MovementResponse rightEast = this.service.getTurn(this.testStarshipName, "right");

        assertNotNull(rightEast);
        assertEquals("OK", rightEast.result());
        assertEquals("Done", rightEast.data().message());
        assertEquals("SOUTH", rightEast.state().heading());
    }

    @Test
    void getTurnRightFacingSouthToWest() {
        this.testStarship.setHeading(Heading.SOUTH);
        NavigationService.MovementResponse rightSouth = this.service.getTurn(this.testStarshipName, "right");

        assertNotNull(rightSouth);
        assertEquals("OK", rightSouth.result());
        assertEquals("Done", rightSouth.data().message());
        assertEquals("WEST", rightSouth.state().heading());
    }

    @Test
    void getTurnRightFacingWestToNorth() {
        this.testStarship.setHeading(Heading.WEST);

        NavigationService.MovementResponse rightWest = this.service.getTurn(this.testStarshipName, "right");

        assertNotNull(rightWest);
        assertEquals("OK", rightWest.result());
        assertEquals("Done", rightWest.data().message());
        assertEquals("NORTH", rightWest.state().heading());
    }

    //**Testing when callsign's movements for each facing heading thrust and backward when valid*/

    @Test
    void move_facingNorthForward_incrementsYAndReturnsDone() {
        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 5, "thrust");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(5, 10);
        assertEquals(newPos, thrustNorth.state().position());

    }

    @Test
    void move_facingSouthForwardDecrementsY(){
        this.testStarship.setHeading(Heading.SOUTH);

        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 3, "thrust");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(5, 2);
        assertEquals(newPos, thrustNorth.state().position());
    }

    @Test
    void move_facingEastForwardIncrementsX(){
        this.testStarship.setHeading(Heading.EAST);

        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 5, "thrust");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(10,5);
        assertEquals(newPos, thrustNorth.state().position());
    }

    @Test
    void move_facingWestForwardDecrementsX(){
        this.testStarship.setHeading(Heading.WEST);

        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 5, "thrust");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(0,5);
        assertEquals(newPos, thrustNorth.state().position());
    }

    @Test
    void move_facingNorthBackwardDecrementsY(){
        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 4, "back");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(5,1);
        assertEquals(newPos, thrustNorth.state().position());
    }

    @Test
    void move_facingSouthBackwardIncrementsY(){
        this.testStarship.setHeading(Heading.SOUTH);

        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 5, "back");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(5,10);
        assertEquals(newPos, thrustNorth.state().position());
    }

    @Test
    void move_facingEastBackwardDecrementsX(){
        this.testStarship.setHeading(Heading.EAST);

        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 5, "back");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(0,5);
        assertEquals(newPos, thrustNorth.state().position());
    }

    @Test
    void move_facingWestBackwardIncrementsX(){
        this.testStarship.setHeading(Heading.WEST);

        NavigationService.MovementResponse thrustNorth = this.service.move(this.testStarshipName, 5, "back");

        assertNotNull(thrustNorth);
        assertEquals("OK", thrustNorth.result());
        assertEquals("Done", thrustNorth.data().message());
        List<Integer> newPos = List.of(10,5);
        assertEquals(newPos, thrustNorth.state().position());
    }
    //**Testing when callsign's movements are collisions/obstructed by hazards / callsigns */

    @Test
    void move_thrustPathBlockedBySpaceHazardStopsOneStepBeforeSpaceHazard(){
        sector.addSpaceHazard(new Asteroid(5,7));

        NavigationService.MovementResponse beforeSpaceHazard = this.service.move(this.testStarshipName,5,"thrust");

        assertNotNull(beforeSpaceHazard);
        assertEquals("OK", beforeSpaceHazard.result());
        assertEquals("Obstructed", beforeSpaceHazard.data().message());
        assertEquals(List.of(5,6), beforeSpaceHazard.state().position());
    }

    @Test
    void move_thrustPathBlockedByOtherStarshipStopsOneStepBeforeStarship(){
        this.testStarship2.setPosition(new Position(5, 8));
        NavigationService.MovementResponse beforeStarship = this.service.move(this.testStarshipName,5,"thrust");

        assertNotNull(beforeStarship);
        assertEquals("OK", beforeStarship.result());
        assertEquals("Obstructed", beforeStarship.data().message());
        assertEquals(List.of(5,7), beforeStarship.state().position());

    }

    @Test
    void move_multiStepMoveHitsBoundaryStopsAtEdge(){
        NavigationService.MovementResponse beforeBoundary = this.service.move(this.testStarshipName2,25,"back");

        assertNotNull(beforeBoundary);
        assertEquals("OK",beforeBoundary.result());
        assertEquals("Obstructed",beforeBoundary.data().message());
        assertEquals(List.of(15,0),beforeBoundary.state().position());

    }

    @Test
    void move_unobstructedBySelfSuccessfullyIgnoresOwnCollisionBox(){
        NavigationService.MovementResponse samePosition = this.service.move(this.testStarshipName2,0,"back");

        assertNotNull(samePosition);
        assertEquals("OK",samePosition.result());
        assertEquals("Done", samePosition.data().message());
        assertEquals(List.of(15,15),samePosition.state().position());

    }


}