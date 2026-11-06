package io.github.thatayaone910.voidfront.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class RequestsTests {
    /*This tests class just tests the json string formating for each request that is made for each command*/
    // create varibles for test cases ti reduece rduncancy
    //for arg specific commands, just overwrite empty mapper with neccesarry args

    private String callsign, shipClass;
    @BeforeEach
    void setUp(){
        callsign = "testStarship";
        shipClass = "sniper";
    }

    @Test
    void testCorrectLaunchRequest() throws JsonProcessingException {
        Requests.CommandRequest deploy= Requests.deployRequest(callsign,shipClass);

        assertEquals(callsign,deploy.callsign());
        assertEquals("deploy",deploy.command());

        assertEquals(shipClass,deploy.arguments().get(0));
        assertEquals(1,deploy.arguments().get(1));
        assertEquals(5,deploy.arguments().get(2));
    }

    @Test
    void testCorrectStateRequest(){
        Requests.CommandRequest state = Requests.stateRequest(callsign);

        assertEquals(callsign,state.callsign());
        assertEquals("state",state.command());
        assertEquals(0,state.arguments().size());
    }

    @Test
    void testCorrectLookRequest(){
        Requests.CommandRequest scan = Requests.scanRequest(callsign);

        assertEquals(callsign,scan.callsign());
        assertEquals("scan",scan.command());
        assertEquals(0,scan.arguments().size());
    }

    @Test
    void testCorrectOrientationRequest(){
        Requests.CommandRequest heading = Requests.headingRequest(callsign);

        assertEquals(callsign,heading.callsign());
        assertEquals("heading",heading.command());
        assertEquals(0,heading.arguments().size());
    }


    @Test
    void testCorrectTurnLeftRequestSize(){
        Requests.CommandRequest rotate = Requests.rotateRequest(callsign,"left");

        assertEquals(callsign,rotate.callsign());
        assertEquals("rotate",rotate.command());
        assertEquals(1,rotate.arguments().size());
    }

    @Test
    void testTurnLeftArgument(){
        Requests.CommandRequest rotate = Requests.rotateRequest(callsign,"left");

        assertEquals(callsign,rotate.callsign());
        assertEquals("rotate",rotate.command());
        assertEquals("left",rotate.arguments().get(0));
    }

    @Test
    void testCorrectTurnRightRequestSize(){
        Requests.CommandRequest rotate = Requests.rotateRequest(callsign,"right");

        assertEquals(callsign,rotate.callsign());
        assertEquals("rotate",rotate.command());
        assertEquals(1,rotate.arguments().size());
    }

    @Test
    void testTurnRightArgument(){
        Requests.CommandRequest rotate = Requests.rotateRequest(callsign,"right");

        assertEquals(callsign, rotate.callsign());
        assertEquals("rotate", rotate.command());
        assertEquals("right", rotate.arguments().get(0));
    }

    @Test
    void testInvalidTurnHeading(){
        Requests.CommandRequest invalid= Requests.rotateRequest(callsign,"upside-down");
        assertNull(invalid);
    }

    @Test
    void testForwardHasDistanceWithArg(){
        Requests.CommandRequest thrust = Requests.thrustRequest(callsign,3);

        assertEquals(callsign,thrust.callsign());
        assertEquals("thrust",thrust.command());
        assertEquals(1,thrust.arguments().size());
        assertEquals(3,thrust.arguments().get(0));
    }

    @Test

    void testBackHasDistanceWithArg(){
        Requests.CommandRequest back = Requests.reverseRequest(callsign,3);

        assertEquals(callsign,back.callsign());
        assertEquals("reverse",back.command());
        assertEquals(1,back.arguments().size());
        assertEquals(3,back.arguments().get(0));
    }

    @Test
    void testNegativeBackDistanceArg(){
        Requests.CommandRequest back = Requests.reverseRequest(callsign, -3);
        assertNull(back);
    }

    @Test
    void testNegativeForwardDistanceArg(){
        Requests.CommandRequest thrust = Requests.thrustRequest(callsign,-7);
        assertNull(thrust);
    }

    @Test
    void testCorrectFireRequest(){
        Requests.CommandRequest fire = Requests.fireRequest(callsign);

        assertEquals(callsign,fire.callsign());
        assertEquals("fire",fire.command());
        assertEquals(0,fire.arguments().size());
    }

    @Test
    void testCorrectReloadRequest(){
        Requests.CommandRequest rearm = Requests.rearmRequest(callsign);

        assertEquals(callsign,rearm.callsign());
        assertEquals("rearm",rearm.command());
        assertEquals(0,rearm.arguments().size());
    }

    @Test
    void testCorrectRepairRequest(){
        Requests.CommandRequest repair = Requests.repairRequest(callsign);

        assertEquals(callsign,repair.callsign());
        assertEquals("repair",repair.command());
        assertEquals(0,repair.arguments().size());
    }

    @Test
    void testCorrectQuitRequest(){
        Requests.CommandRequest quit = Requests.quitRequest(callsign);

        assertEquals(callsign,quit.callsign());
        assertEquals("quit",quit.command());
        assertEquals(0,quit.arguments().size());
    }

}
