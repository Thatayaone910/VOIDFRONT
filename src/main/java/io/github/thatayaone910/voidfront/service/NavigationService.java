package io.github.thatayaone910.voidfront.service;

import io.github.thatayaone910.voidfront.sector.Heading;
import io.github.thatayaone910.voidfront.sector.Position;
import io.github.thatayaone910.voidfront.sector.Starship;
import io.github.thatayaone910.voidfront.sector.Sector;
import io.github.thatayaone910.voidfront.sector.hazard.SpaceHazard;
import io.github.thatayaone910.voidfront.sector.hazard.VoidRift;

import java.util.List;

/** This service class handles movement commands for starships in the sector.
 *  It supports two commands: rotate (left/right) and move (thrust/back).
 */
public class NavigationService {
    private Sector sector;

    public record MovementResponse(String result,Data data,QueryService.State state){}
    public record Data(String message){}


    public NavigationService(Sector sector) {
        this.sector = sector;
    }


    /**
     * Turns a starship left or right without moving it, updating only its facing heading.
     *
     * @param callsign     The name of the starship to rotate.
     * @param rotateHeading The heading to rotate, either "left" or "right".
     * @return A TurnResponse containing the result, new facing heading, and a message.
     * Returns null if the starship is not found or the rotate heading is invalid.
     */

    public MovementResponse getTurn(String callsign, String rotateHeading) {
        synchronized (sector) {
            return getTurnLocked(callsign, rotateHeading);
        }
    }

    private MovementResponse getTurnLocked(String callsign, String rotateHeading) {
        //find if starship is in the sector first
        Starship moveStarship = sector.findStarship(callsign);

        if (moveStarship == null) {
            return null;
        }
        if ("DEAD".equalsIgnoreCase(moveStarship.getStatus())) return null;
        switch (rotateHeading) {
            case "left":
                switch (moveStarship.getHeading()) {
                    case NORTH -> moveStarship.rotateTo(Heading.WEST);
                    case WEST -> moveStarship.rotateTo(Heading.SOUTH);
                    case SOUTH -> moveStarship.rotateTo(Heading.EAST);
                    case EAST -> moveStarship.rotateTo(Heading.NORTH);
                }
                break;

            case "right":
                switch (moveStarship.getHeading()) {
                    case NORTH -> moveStarship.rotateTo(Heading.EAST);
                    case WEST -> moveStarship.rotateTo(Heading.NORTH);
                    case SOUTH -> moveStarship.rotateTo(Heading.WEST);
                    case EAST -> moveStarship.rotateTo(Heading.SOUTH);
                }
                break;
        }

        return new MovementResponse("OK",new Data("Done"),
                new QueryService.State(List.of(moveStarship.getPosition().getX(),
                        moveStarship.getPosition().getY()),
                        moveStarship.getHeading().toString(),
                        moveStarship.getShieldStrength(),
                        moveStarship.getShots(),
                        moveStarship.getStatus(),
                        moveStarship.getKillCount()
                ));
    }

    /**
     * Moves a starship thrust or backward by a given number of steps.
     * The starship stops early and returns "Obstructed" if it hits a sector boundary,
     * another starship, or an hazard before completing all steps.
     *
     * @param callsign The name of the starship to move.
     * @param steps     The number of steps to move the starship.
     * @param command   The movement heading, either "thrust" or "back".
     * @return A MoveResponse containing the result, a message ("Done" or "Obstructed"),
     * and the starship's final position. Returns null if the starship is not found.
     */

    public MovementResponse move(String callsign, int steps, String command) {
        synchronized (sector) {
            return moveLocked(callsign, steps, command);
        }
    }

    private MovementResponse moveLocked(String callsign, int steps, String command) {
        Starship moveStarship = sector.findStarship(callsign);
        boolean isObstructed = false;

        if (moveStarship == null) {
            return null;
        }

        if ("DEAD".equalsIgnoreCase(moveStarship.getStatus())) {
            return new MovementResponse("ERROR", new Data("Starship is dead"), null);
        }
        if (moveStarship.isBusy()) {
            return new MovementResponse("ERROR", new Data("Starship is busy"), null);
        }

        boolean fellInVoidRift = false;
        Position nextPos = null;
        for (int i = 0; i < steps; i++) {
            int step = command.equals("thrust") ? 1 : -1;
            int nextX = moveStarship.getPosition().getX() + moveStarship.getHeading().xOffset() * step;
            int nextY = moveStarship.getPosition().getY() + moveStarship.getHeading().yOffset() * step;

            nextPos = new Position(nextX, nextY);
            Position[] bounds = sector.getSectorBounds();
            if (!nextPos.isIn(bounds[0], bounds[1])) {
                isObstructed = true;
                break;
            }

            for (Starship starship : sector.getAllStarships()) {
                if (starship.getCallsign().equals(moveStarship.getCallsign())) continue;

                if (starship.blocksPosition(nextPos)) {
                    isObstructed = true;
                    break;
                }

            }
            if (isObstructed) break;

            fellInVoidRift = false;

            for (SpaceHazard hazard : sector.getSpaceHazards()) {

                if (hazard instanceof VoidRift && hazard.blocksPosition(nextPos)) {
                    moveStarship.setPosition(nextPos);
                    moveStarship.setStatus("DEAD");
                    fellInVoidRift = true;
                    break;
                }

                if (!(hazard instanceof VoidRift) && hazard.blocksPosition(nextPos)) {
                    isObstructed = true;
                    break;
                }
            }

            if (fellInVoidRift) break;
            if (isObstructed) break;

            moveStarship.setPosition(nextPos);
        }

        String message = isObstructed ? "Obstructed" : "Done";
        return new MovementResponse("OK", new Data(message),
                new QueryService.State(List.of(moveStarship.getPosition().getX(),
                        moveStarship.getPosition().getY()),
                        moveStarship.getHeading().toString(),
                        moveStarship.getShieldStrength(),
                        moveStarship.getShots(),
                        moveStarship.getStatus(),
                        moveStarship.getKillCount()));
    }

}




















