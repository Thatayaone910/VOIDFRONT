package io.github.thatayaone910.voidfront.service;

import io.github.thatayaone910.voidfront.sector.*;
import java.util.List;

public class QuitService {
/**
 *Service responsible for handling the quit command
 *<p>
 * When a starship quits, its marked as dead instead of being removed from the sector
 * This allows the server to return the starships final state after quitting
 * */

/**The sectors instance where the starship exists*/
private final Sector sector;
    /**
     * Constructs a new QuitService
     * @param sector The game sector environment
     * */
    public QuitService(Sector sector) {
        this.sector = sector;
    }

    public record QuitResponse(String result, QuitData data, QueryService.State state){}

    public record QuitData(String message){}

    /**
     * Processes a quit request for a specific starship
     * Sets the starship's status to DEAD and returns a final state
     *
     * @param callsign The unique identifier for the starship wishing to quit
     * @return A QuitResponse of the starship's final state or an error response if the starship is not found
     * */
    public Object getQuit(String callsign) {
        synchronized (sector) {
        //find the starship first
        Starship quitter = sector.findStarship(callsign);
        if(quitter==null){
            return QueryService.badMessage();
        }

        if (!quitter.getStatus().equals("DEAD")) {
            quitter.setStatus("DEAD");
        }
        return new QuitResponse("OK",new QuitData(callsign + " has quit the game"),
                new QueryService.State(List.of(quitter.getPosition().getX(),
                        quitter.getPosition().getY()),
                        quitter.getHeading().toString(),
                        quitter.getShieldStrength(),
                        quitter.getShots(),
                        quitter.getStatus(),
                        quitter.getKillCount()));
        }
    }
}
