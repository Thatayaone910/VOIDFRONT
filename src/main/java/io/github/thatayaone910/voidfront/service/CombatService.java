package io.github.thatayaone910.voidfront.service;

import io.github.thatayaone910.voidfront.sector.Position;
import io.github.thatayaone910.voidfront.sector.Starship;
import io.github.thatayaone910.voidfront.sector.Sector;
import io.github.thatayaone910.voidfront.sector.hazard.SpaceHazard;

/**This class handles the combat commands that the starship can use.
 * <p>
 * "fire" - Instructs the starship to fire its gun.<br>
 * "rearm" - Instructs the starship to rearm its weapons.<br>
 * "repair" - Instructs the starship to repair its shields.*/
public class CombatService {
    private final Sector sector;

    /**
     * Constructor for CombatService.
     * @param sector The sector instance containing starship and configuration data.
     */
    public CombatService(Sector sector){
        this.sector = sector;
    }

    /**
     * The main method to handle the `fire` command. It will return a response
     * corresponding to whether a target starship was hit or not.
     * <br>
     * The starship fire in the heading it's currently fcaing.
     * The bullet travels step-by-step until:
     * <ul>
     *     <li>An hazard blocks the shot , misses</li>
     *     <li>An alive starship is hit</li>
     *     <li>The maximum visibilty range is reached , miss</li>
     * </ul>
     * Dead starships are ignored during collion checks
     * A starship cann0t fire while repairing, rearming or dead,
     * or sho shots are left
     *
     * @param firingStarshipName Name of the Starship that's executing the fire command.
     * @return NULL - If there is no starship with the provided name in the sector.<br>
     *         {String} Response for 'Hit' if the target was hit.<br>
     *         {String} Response for 'Miss' if the target was missed.<br>
     * */
    public String fire(String firingStarshipName){
        synchronized (sector) {
            return fireLocked(firingStarshipName);
        }
    }

    private String fireLocked(String firingStarshipName) {
        Starship fireStarship = sector.findStarship(firingStarshipName);
        if( fireStarship == null ||  fireStarship.getStatus().equals("DEAD")) return "{\n" +
                "  \"result\": \"ERROR\",\n" +
                "  \"data\": {\n" +
                "    \"message\": \"Starship not found\"\n" +
                "  }\n" +
                "}";    // starship doesnt exist

        if(fireStarship.getShots()<=0){
            return "{\n" +
                    "  \"result\": \"ERROR\",\n" +
                    "  \"data\": {\n" +
                    "    \"message\": \"Zero shots! Reload now!\"\n" +
                    "  }\n" +
                    "}";
        }

        if(fireStarship.getStatus().equals("REARMING") ||
        (fireStarship.getStatus().equals("REPAIRING"))){
            return "{\n" +
                    "  \"result\": \"ERROR\",\n" +
                    "  \"data\": {\n" +
                    "    \"message\": \"Cannot fire while rearming or repairing\"\n" +
                    "  }\n" +
                    "}";
        }

        for(int distance = 1; distance <= sector.getScanRange();distance++){
            //check the posistion in front of the starship
            Position checkPos = sector.positionInFront(fireStarship,distance);
            //check if hazard block fire bullet
            for(SpaceHazard o: sector.getSpaceHazards()){
                if(o.blocksPosition(checkPos)){
                    fireStarship.decreaseShots();
                    return missResponse(fireStarship.getShots());
                }
            }

            // Find starship in bullet range
            for(Starship targetStarship : sector.getAllStarships()) {
            if(targetStarship == fireStarship) continue;
            if(targetStarship.getStatus().equals("DEAD")) continue;
            if(targetStarship.getShieldStrength()<=0) continue;

            if(targetStarship.blocksPosition(checkPos)){
                fireStarship.decreaseShots();
                targetStarship.decreaseShieldStrength();

                if(targetStarship.getShieldStrength()<=0){
                    targetStarship.setStatus("DEAD");
                    //never let sheilds reach 0
                    targetStarship.setShieldStrength(0);
                    fireStarship.incrementKillCount();
                }
            return hitResponse(fireStarship, targetStarship);}}
        }
        fireStarship.decreaseShots();
        return missResponse(fireStarship.getShots());
    }

    /**
     * Returns a JSON-formatted response for a successful hit.
     *
     * @param queryingStarship The Starship that's executing the fire command.
     * @param targetStarship The targeted Starship to be Hit (or missed).
     * @return {String} JSON formatted string ready to send back to the client.
     * */
    private String hitResponse(Starship queryingStarship, Starship targetStarship){
        return "{\n" +
                "  \"result\": \"OK\",\n" +
                "  \"data\": {\n" +
                "    \"message\": \"Hit\",\n" +
                "    \"distance\": "+ sector.distanceBetween( queryingStarship, targetStarship ) +",\n" +
                "    \"callsign\": \""+ targetStarship.getCallsign() +"\",\n" +
                "    \"state\": {\n" +
                "       \"position\": ["+ targetStarship.getPosition().getX() +","+ targetStarship.getPosition().getY() +"],\n" +
                "       \"heading\": \""+ targetStarship.getHeading().toString() +"\",\n" +
                "       \"shields\": "+ targetStarship.getShieldStrength() +",\n" +
                "       \"shots\": "+ targetStarship.getShots() +",\n" +
                "       \"status\": \""+ targetStarship.getStatus() +"\"\n" +
                "    }" +
                "  },\n" +
                "  \"state\": {\n" +
                "    \"shots\":  " + queryingStarship.getShots() + ",\n" +
                "    \"killCount\": " + queryingStarship.getKillCount() + "\n" +
                "  }\n" +
                "}";

    }

    /**
     * Returns a JSON-formatted response for a missed shot.
     *
     * @param remainingShots The updated number of shots left.
     * @return {String} JSON formatted string ready to send back to the client.
     * */
    private String missResponse(int remainingShots){
        return "{\n" +
                "  \"result\": \"OK\",\n" +
                "  \"data\": {\n" +
                "    \"message\": \"Miss\"\n" +
                "  },\n" +
                "  \"state\": {\n" +
                "    \"shots\":  " + remainingShots + "\n" +
                "  }\n" +
                "}";
    }

    /**
     * Handles the 'rearm' command. Blocks the thread for 3 seconds.
     * @param queryingStarshipName Name of the Starship.
     */
    public String rearm(String queryingStarshipName) {
        Starship starship;
        synchronized (sector) {
            starship = sector.findStarship(queryingStarshipName);

            if (starship == null || starship.getStatus().equals("DEAD")) {
                return "{\n  \"result\": \"ERROR\",\n  \"data\": {\n    \"message\": \"Starship not found\"\n  }\n}";
            }

            if (starship.isBusy()) {
                return "{\n  \"result\": \"ERROR\",\n  \"data\": {\n    \"message\": \"Starship is busy\"\n  }\n}";
            }

            if (starship.getShots() == starship.getMaxShots()) {
                return "{\n" +
                        "  \"result\": \"ERROR\",\n" +
                        "  \"data\": {\n" +
                        "    \"message\": \"Shots at maximum capacity\"\n" +
                        "  }\n" +
                        "}";
            }

            starship.setStatus("REARMING");
        }
        try {
            Thread.sleep(sector.getRearmDuration() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        synchronized (sector) {
            if ("DEAD".equalsIgnoreCase(starship.getStatus())) {
                return "{\n  \"result\": \"ERROR\",\n  \"data\": {\n    \"message\": \"Starship is dead\"\n  }\n}";
            }
            starship.rearmShots();
            starship.setStatus("NORMAL");
        }

        return "{\n" +
                "  \"result\": \"OK\",\n" +
                "  \"data\": {\n" +
                "    \"message\": \"Done\"\n" +
                "  },\n" +
                "  \"state\": {\n" +
                "    \"position\": [" + starship.getPosition().getX() + "," + starship.getPosition().getY() + "],\n" +
                "    \"heading\": \"" + starship.getHeading() + "\",\n" +
                "    \"shields\": " + starship.getShieldStrength() + ",\n" +
                "    \"shots\": " + starship.getShots() + ",\n" +
                "    \"status\": \"" + starship.getStatus() + "\",\n" +
                "    \"killCount\": " + starship.getKillCount()+"\n" +
                "  }\n" +
                "}";
    }

    /**
     * Handles the 'repair' command. Restores shields to maximum strength.
     * The starship is unable to execute other commands during the repair duration
     * defined in config.properties.
     *
     * @param queryingStarshipName Name of the Starship that's executing the repair.
     * @return JSON formatted string indicating success or error.
     */
    public String repair(String queryingStarshipName){

        Starship starship;
        synchronized (sector) {
            starship = sector.findStarship(queryingStarshipName);

            if (starship == null || starship.getStatus().equalsIgnoreCase("DEAD")) {
                return "{\n" +
                        "  \"result\": \"ERROR\",\n" +
                        "  \"data\": {\n" +
                        "    \"message\": \"Starship not found\"\n" +
                        "  }\n" +
                        "}";
            }

            if (starship.isBusy()) {
                return "{\n  \"result\": \"ERROR\",\n  \"data\": {\n    \"message\": \"Starship is busy\"\n  }\n}";
            }

            if (starship.getShieldStrength() >= starship.getMaxShieldStrength()) {
                return "{\n" +
                        "  \"result\": \"ERROR\",\n" +
                        "  \"data\": {\n" +
                        "    \"message\": \"Shields at maximum capacity\"\n" +
                        "  }\n" +
                        "}";
            }

            starship.setStatus("REPAIRING");
        }
        try {
            // Get repair time from sector config and block thread (multiplied by 1000 for milliseconds)
            int repairTime = sector.getRepairDuration() * 1000;
            Thread.sleep(repairTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Repair interrupted for: " + queryingStarshipName);
        }

        synchronized (sector) {
            if ("DEAD".equalsIgnoreCase(starship.getStatus())) {
                return "{\n  \"result\": \"ERROR\",\n  \"data\": {\n    \"message\": \"Starship is dead\"\n  }\n}";
            }
            starship.setShieldStrength(starship.getMaxShieldStrength());
            starship.setStatus("NORMAL");
        }

        return "{\n" +
                "  \"result\": \"OK\",\n" +
                "  \"data\": {\n" +
                "    \"message\": \"Done\"\n" +
                "  },\n" +
                "  \"state\": {\n" +
                "    \"position\": [" + starship.getPosition().getX() + "," + starship.getPosition().getY() + "],\n" +
                "    \"heading\": \"" + starship.getHeading() + "\",\n" +
                "    \"shields\": " + starship.getShieldStrength() + ",\n" +
                "    \"shots\": " + starship.getShots() + ",\n" +
                "    \"status\": \"" + starship.getStatus() + "\",\n" +
                "    \"killCount\": " + starship.getKillCount()+"\n" +
                "  }\n" +
                "}";
    }
}
