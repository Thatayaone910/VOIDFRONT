package io.github.thatayaone910.voidfront.protocol;

import io.github.thatayaone910.voidfront.sector.ShipClass;
import java.util.List;
import java.util.Collections;

/**
 * This class created standard JSON request objects for starship commands*/
public class Requests {
    /**
     * Represents the structure of all JSON requests
     * @param callsign identifies the starship sending a request
     * @param command is the specific action requested e.g.deploy,state, rotate, fire
     * @param arguments is a list the argument values required by the command
     * */
    public record CommandRequest(String callsign,String command,List<Object> arguments) {}

    /**
     * This method creates a request to deploy a new starship into the sector
     * @param callsign name to assign the starship
     * @param shipClass the type of starship to deploy e.g. sniper, tank
     * @return a {@link CommandRequest} object, or {@code null} if inputs are invalid
     * */
    public static CommandRequest deployRequest(String callsign, String shipClass){
        List<Object> kind = ShipClass.getShipClass(shipClass);
        //returns a record JSON of "callsign":"adc","command":"deploy","arguments":["sniper",2,5]
        if(!shipClass.isEmpty() && !callsign.isEmpty()){
            return new CommandRequest(callsign,"deploy",kind);
        }
        return null;
    }

    /**
     * Creates a request to retrieve the current state of the starship
     * @param callsign the starship's name making a state request
     * @return a {@link CommandRequest} for the "state" command
     * */
    public static CommandRequest stateRequest(String callsign) {
        //returns a JSON of "callsign":"adc","command":"state","arguments":[]
        return new CommandRequest(callsign,"state",Collections.emptyList());
    }

    /**
     * Creates a request for the starship to scan at its surroundings
     * @param callsign the starship's name making a state request
     * @return a {@link CommandRequest} for the "scan" command
     * */
    public static CommandRequest scanRequest(String callsign){
        //returns a JSON of "callsign":"adc","command":"scan","arguments":[]
        return new CommandRequest(callsign,"scan",Collections.emptyList());
    }

    /**
     * This method builds a record 'map' and sets the fields for the state command
     * @param callsign is a string value the starship's name making a state request
     * @return a request JSON, a record of the starship's name, command and empty is for arguments*/
    public static CommandRequest headingRequest(String callsign){
        //returns a JSON of "callsign":"adc","command":"heading","arguments":[]
        return new CommandRequest(callsign,"heading",Collections.emptyList());
    }

    /**
     * Creates a request for a starship to quit the game
     * @param callsign The starship wanting to quit the game
     * @return a {@link CommandRequest} for the "quit" command
     * */
    public static  CommandRequest quitRequest(String callsign){
        //returns a JSON of "callsign":"adc","command":"quit","arguments":[]
        return new CommandRequest(callsign,"quit",Collections.emptyList());
    }

    /**
     * Creates a request to rotate a starship to a specified heading, left or right
     * @param callsign the starship's name making a thrust request
     * @param heading The heading to rotate a starship e.g."left" or "right"
     * @return a {@link CommandRequest} for the "thrust" command
     * */
    public static CommandRequest rotateRequest(String callsign, String heading) {
        //returns a JSON of "callsign":"adc","command":"rotate","arguments":["left"] or ["right"]
        if(heading.equalsIgnoreCase("left") || heading.equalsIgnoreCase("right")){
            return new CommandRequest(callsign,"rotate",List.of(heading));
        }
        return null;
    }

    /**
     * Creates a request to move a starship thrust by a set number of steps
     * @param callsign the starship's name making a thrust request
     * @param steps the number of steps to move, greater than 0
     * @return a {@link CommandRequest} for the "thrust" command */
    public static CommandRequest thrustRequest(String callsign, int steps) {
        //returns a JSON of "callsign":"adc","command":"state","arguments":[5]
        if(steps>0){ return new CommandRequest(callsign,"thrust", List.of(steps));}
        return null;
    }

    /**
     * Creates a request to move a starship back by a set number of steps
     * @param callsign The starship's name making a back request
     * @param steps The number of steps to move, greater than 0
     * @return a {@link CommandRequest} for the "back" command
     */
    public static CommandRequest reverseRequest(String callsign, int steps) {
        if(steps>0){         return new CommandRequest(callsign,"reverse", List.of(steps));}
        return null;
    }

    /**
     * Creates a request to fire the starship's weapon
     * @param callsign the starship's name making a fire request
     * @return a {@link CommandRequest} for the "fire" command
     */
    public static CommandRequest fireRequest(String callsign) {
        return new CommandRequest(callsign,"fire",Collections.emptyList());
    }

    /**
     * Creates a request to rearm the starship's weapon
     * @param callsign the starship's name making a rearm request
     * @return a {@link CommandRequest} for the "rearm" command
     * */
    public static CommandRequest rearmRequest(String callsign) {
        return new CommandRequest(callsign,"rearm",Collections.emptyList());
    }

    /**
     * This method builds a record 'map' and sets the fields for the repair command
     * @param callsign is a string value the starship's name making a repair request
     * @return a request JSON, a record of the starship's name, command and empty is for arguments*/
    public static CommandRequest repairRequest(String callsign) {
        return new CommandRequest(callsign,"repair",Collections.emptyList());
    }

}
