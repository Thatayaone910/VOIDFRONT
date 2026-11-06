package io.github.thatayaone910.voidfront.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.thatayaone910.voidfront.sector.*;

import java.util.List;
import java.util.Locale;

/**
 * Service responsible for handling the 'deploy' command
 * It manages the initialization of a new starship, places it within the sector
 * and constructs the initial data and state for the starship
 * */
public class DeployService {
    private final Sector sector;

    /**
     * Constructs the LaunchResponse with a reference to the game sector
     * @param sector The sector instance where the starship will be deployed*/
    public DeployService(Sector sector){ this.sector =sector;
    }


    /**
     * Represents the successful response wrapper for a deploy command
     * @param result The outcome of the request, "OK"
     * @param data Configuration and sector constraints for teh starship
     * @param state The physical state of the newly deployed starship
     * */
    public record DeployResponse(String result, DeployData data,DeployState state){}


    /**
     * Represents static data and  sector rules provided upon deploy
     * @param position Initial coordinates [x,y]
     * @param heading Initial compass heading
     * @param visibility The sector's visibility range
     * @param rearm The rearm time required to rearm weapons
     * @param repair The time required to repair shields
     * @param sectorBounds The sector's width and height
     * @param shots Initial ammo count*/
    public record DeployData(List<Integer> position,
                             String heading,
                             int visibility,
                             int rearm,
                             int repair,
                             Position[] sectorBounds,
                             int shots){}
    public record ErrorData(String message){}


    /**
     * Represents the dynamic state of the starship at the moment of deploy.
     *
     * @param position  Current coordinates [x, y].
     * @param heading Current facing heading.
     * @param shields   Current shield strength.
     * @param shots     Current ammo count.
     * @param status    Current status (e.g., "NORMAL").
     * @param killCount
     */
    public record DeployState(List<Integer> position,
                              String heading,
                              int shields,
                              int shots,
                              String status, int killCount){}


    /**
     * Represents an error message  if the deploy fails
     * @param data Description of why the deploy couldn't be deployed*/
    public record DeployError(String result,ErrorData data){}

    /**
     * Processes the deploy of a new starship into the sector.
     * Creates a new Starship instance, adds it to the sector's active starships,
     * and compiles the initial environment data.
     *
     * @param callsign The name provided by the client.
     * @param arguments JSON node containing deploy arguments (e.g., starship make/type).
     * @return A DeployResponse object containing the initial sector data and starship state.
     * @throws JsonProcessingException if the arguments cannot be parsed correctly.*/
    public Object deployStarship(String callsign,JsonNode arguments) throws JsonProcessingException {
        synchronized (sector) {
            return deployStarshipLocked(callsign, arguments);
        }
    }

    private Object deployStarshipLocked(String callsign, JsonNode arguments) throws JsonProcessingException {
        if (callsign == null || callsign.isBlank()) {
            return new DeployError("ERROR", new ErrorData("Callsign cannot be empty"));
        }
        if (!hasValidArguments(arguments)) {
            return new DeployError("ERROR", new ErrorData("Invalid deployment arguments"));
        }
        String normalizedCallsign = callsign.toUpperCase(Locale.ROOT);
        if (sector.findStarship(normalizedCallsign) != null) {
                return new DeployError("ERROR",new ErrorData("Too many of you in this sector"));
        }

        Starship newStarship = sector.addStarship(
                new Starship(normalizedCallsign, arguments, sector.randomAvailablePosition()));

        List<Integer> position = List.of(newStarship.getPosition().getX(),newStarship.getPosition().getY());
        DeployData data = new DeployData(position,
                newStarship.getHeading().toString(),
                sector.getScanRange(),
                sector.getRearmDuration(),
                sector.getRepairDuration(),
                sector.getSectorBounds(),
                newStarship.getShots());

        DeployState state=new DeployState(position,
                newStarship.getHeading().toString(),
                newStarship.getShieldStrength(),
                newStarship.getShots(),
                newStarship.getStatus(),
                newStarship.getKillCount());

        return new DeployResponse("OK",data,state);
    }

    private boolean hasValidArguments(JsonNode arguments) {
        return arguments != null
                && arguments.isArray()
                && arguments.size() == 3
                && arguments.get(0).isTextual()
                && ShipClass.isSupportedShipClass(arguments.get(0).asText())
                && arguments.get(1).isIntegralNumber()
                && arguments.get(1).canConvertToInt()
                && arguments.get(1).asInt() >= 0
                && arguments.get(2).isIntegralNumber()
                && arguments.get(2).canConvertToInt()
                && arguments.get(2).asInt() >= 0;
    }
}
