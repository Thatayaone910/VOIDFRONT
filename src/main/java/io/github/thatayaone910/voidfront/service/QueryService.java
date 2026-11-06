package io.github.thatayaone910.voidfront.service;


import io.github.thatayaone910.voidfront.sector.*;
import io.github.thatayaone910.voidfront.sector.hazard.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class that handles sector-specific starship query commands
 * It processes client requests for starship status and sector environment information without modifying
 * its own and the sector's state*/
public class QueryService {
    /**The sector instance being queried*/
    private Sector sector;


    /**
     * Constructs a new QueryService for the given sector
     * @param sector The sector environment to query
     * */
    public QueryService(Sector sector){
        this.sector = sector;
    }


    /**Record representing a successful starship state response*/
    public record StateResponse(String result, State state){}
        /**Record containing the starships state*/
    public record State(List<Integer> position, String heading, int shields, int shots, String status, int killCount){}

    /** Record representing an error message*/
    public record ErrorMessage(String result,ErrorData data){}

    /** Record containing the error message detail*/
    public record ErrorData(String message){}

    /**
     * Method to create a standard error message when a starship is not found
     * Used for state, heading and scan queries
     * @return  An ErrorMessage containing "Starship not found"
     * */
    public static ErrorMessage badMessage(){
        return new ErrorMessage("ERROR",new ErrorData("Starship not found"));
    }

    /**Record representing a successful starship heading response*/
    public record HeadingResponse(String result,String heading){}

    /**Record representing a successful starship scan response*/
    public record ScanResponse(String result, ScanData data, State state){}

    /**Record containing all visible sector objects*/
    public record ScanData(List<ScanContact> objects){}

    /**Record representing a visible object in the sector*/
    public record ScanContact(String heading, String type, int distance,String name){}

    /**
     * Retrieves a starship's current heading, where it is currently facing
     * @param callsign The unique identifier of the starship
     * @return  An HeadingResponse if found, otherwise an ErrorMessage
     * */
    public Object getHeading(String callsign){
        synchronized (sector) {
            return getHeadingLocked(callsign);
        }
    }

    private Object getHeadingLocked(String callsign) {
        Starship queryStarship = sector.findStarship(callsign);

        if (queryStarship== null){
            return badMessage();
        }
        return new HeadingResponse("OK",queryStarship.getHeading().name());
    }

    /**
     * Retrives a starship's complete state and position
     *
     * @param callsign The unique starship identifier
     * @return A StateResponse containing position, heading, shields, shots and status
     * */
    public Object getState(String callsign){
        synchronized (sector) {
            return getStateLocked(callsign);
        }
    }

    private Object getStateLocked(String callsign) {
        //find if starship is in the sector first
        Starship queryStarship = sector.findStarship(callsign);

        if (queryStarship==null){
            return badMessage();
        }

        //get starship details using starship class methods
        return new StateResponse("OK",new State(
                List.of(queryStarship.getPosition().getX(),
                        queryStarship.getPosition().getY()),
                queryStarship.getHeading().toString(),
                queryStarship.getShieldStrength(),
                queryStarship.getShots(),
                queryStarship.getStatus(),
                // Include kill count in the starship's state response
                queryStarship.getKillCount()));
    }


    /**Scans the sector for hazards and other starships within visibility as well as any sector edges
     * Dead starships are ignored during visibilty checks.
     *
     * @param callsign Name of the starship querying the sector
     * @return  A ScanResponse containing visible sector objects and querying starship current state*/

    public Object getScan(String callsign) {
        synchronized (sector) {
            return getScanLocked(callsign);
        }
    }

    private Object getScanLocked(String callsign) {
        //find if starship is in the sector first
        Starship queryStarship = sector.findStarship(callsign);
        if (queryStarship == null) {
            return badMessage();
        }

        List<ScanContact> visibleObjects = new ArrayList<>();
        //loop through all the starships
        for (Starship starship2 : sector.getAllStarships()) {
            if (starship2==queryStarship) continue;

            if(starship2.getStatus().equals("DEAD")) continue;

            Heading heading = sector.validHeading(queryStarship, starship2.getPosition());
            if (heading == null) continue; //diagonal starship

            int distance = sector.distanceBetween(queryStarship, starship2);

            if (distance <= sector.getScanRange()) {
                //add if the starship is within visibility range and up, down, left, right
                visibleObjects.add(new ScanContact(heading.toString(), "STARSHIP", distance,starship2.getCallsign()));
            }
        }
        // loop through all the hazards
        for(SpaceHazard o: sector.getSpaceHazards()){
            Heading heading = sector.validHeading(queryStarship,o.getPosition());
            if(heading==null)continue;

            int distance = sector.distanceToSpaceHazard(queryStarship,o);
            if(distance <=sector.getScanRange()){
            visibleObjects.add(
                    //add if hazard is within visbilty and is left/right/above/below
                    new ScanContact(heading.toString(),"HAZARD",distance,o.getType()));
        }}

        for(Heading heading: Heading.values()){
            int distance = sector.distanceToEdge(queryStarship, heading);
            if(distance<=sector.getScanRange()){
                //if starship is near an edge within visiblity , add it to visible objects
                visibleObjects.add(new ScanContact(heading.name(), "EDGE",distance, "EDGE"));
            }
        }

        return new ScanResponse("OK",
                new ScanData(visibleObjects),
                new State(List.of(queryStarship.getPosition().getX(),
                        queryStarship.getPosition().getY()),
                        queryStarship.getHeading().toString(),
                        queryStarship.getShieldStrength(),
                        queryStarship.getShots(),
                        queryStarship.getStatus(),
                        queryStarship.getKillCount()));
    }
}
