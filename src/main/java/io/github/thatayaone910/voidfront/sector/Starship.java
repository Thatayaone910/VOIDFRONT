package io.github.thatayaone910.voidfront.sector;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class represents a starship/player in the Sector. It encapsulates all the attributes
 * and behaviours that a player has.
 * */
public class Starship {

    private final String callsign;
    private final String shipClass;
    private Position position;
    private Heading heading;
    private int shieldStrength;
    private final int maxShieldStrength;
    private int shots;
    private final int maxShots;
    private String status;
    private int killCount;

    /**
     * This allows the construction of a Starship object when 'deployed' into the Sector. The Starship will be
     * instantiated with the provided values, and then set the other values to default.
     *
     * @param name The name of the Starship it will be identified by in the Sector.
     * @param arguments The rest of the arguments provided with the 'deploy' command.
     * @throws JsonProcessingException When there was a problem with extracting or parsing the values in 'arguments'.
     * */
    public Starship(String callsign, JsonNode arguments) throws JsonProcessingException {
        this(callsign, arguments, new Position(0, 0));
    }

    public Starship(String callsign, JsonNode arguments, Position initialPosition) throws JsonProcessingException {
        // Provided values
        this.callsign = callsign;
        this.shipClass = arguments.get(0).asText();
        this.shieldStrength = arguments.get(1).asInt();
        this.shots = arguments.get(2).asInt();
        this.maxShieldStrength = this.shieldStrength;
        this.maxShots = this.shots;

        // Default values
        this.status = "NORMAL";
        this.killCount = 0;
        this.heading = Heading.NORTH;
        this.position = initialPosition;

    }


    public Starship(String callsign, String shipClass, JsonNode state) throws JsonProcessingException {
        this.callsign = callsign;
        this.shipClass = shipClass;
        this.maxShieldStrength = Integer.parseInt( state.get("shields").asText() );
        this.maxShots = Integer.parseInt( state.get("shots").asText() );
        this.shieldStrength = maxShieldStrength;
        this.shots = maxShots;
        this.status = state.get("status").asText();
        this.heading = Heading.valueOf( state.get("heading").asText() );
        this.position = new Position(
                Integer.parseInt( state.get("position").get(0).asText() ),
                Integer.parseInt( state.get("position").get(1).asText() )
        );
        this.killCount = state.has("killCount") ? state.get("killCount").asInt() : 0;
    }

    /**
     * Provides the name of the Starship.
     *
     * @return The given name as a string.
     * */
    public String getCallsign() {
        return this.callsign;
    }

    /**
     * Provides the Kind of the Starship.
     *
     * @return The Starship kind as a string.
     * */
    public String getShipClass() {
        return this.shipClass;
    }

    /**
     * Provides the current Status of the Starship.
     *
     * @return The Status as a string.
     * */
    public String getStatus() {
        return this.status;
    }

    /**
     * Updates the Status of the Starship.
     *
     * @param status The new starship Status.
     * */
    public void setStatus(String status) {
        this.status = status;
    }
    /**Freezes the starship when repairing or rearming*/
    public boolean isBusy() {
        return status.equals("REARMING") || status.equals("REPAIRING");
    }

    /**
     * Provides the current heading/heading of the Starship.
     *
     * @return The starship's heading.
     * */
    public Heading getHeading() {
        return this.heading;
    }

    /**
     * Updates the heading which the starship should face.
     *
     * @param heading The new heading enum.
     * */
    public void rotateTo(Heading heading) {
        if(heading!=null) {
            this.heading = heading;
        }
    }

    /**
     * Provides the Starship's current position in the Sector.
     *
     * @return The starship's position.
     * */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Checks whether the starship is sitting at a certain position.
     *
     * @param pos The position to evaluate (It must be within the bounds of the Sector).
     * @return True -> The starship is sitting directly at that position<br>
     *         False -> The starship is not at the position.
     * */
    public boolean blocksPosition(Position pos) {
        return pos.getX() == this.position.getX() && pos.getY() == this.position.getY();
    }

    /**
     * Moves the starship thrust to a new position by taking a certain number of steps
     * in the heading it is currently facing.
     *
     * @param nrSteps The number of steps the starship must take.
     * @return True -> The starship was moved to the new position<br>
     *         False -> Something is blocking the starship from moving thrust.
     * */
    /**
     * Updates the current shield strength of the starship.
     * @param strength The new shield value.
     */
    public void setShieldStrength(int strength) {
        this.shieldStrength = strength; // Ensure your variable is named shieldStrength
    }

    /**
     * Provides the default shield strength provided during instantiation.
     *
     * @return Maximum shield strength.
     * */
    public int getMaxShieldStrength() {
        return this.maxShieldStrength;
    }

    /**
     * Provides the current shield strength of the starship.
     *
     * @return current shield strength.
     * */
    public int getShieldStrength() {
        return this.shieldStrength;
    }

    /**
     * @param shield The new shield value
     * */
    public void setShield(int shield) {
        this.shieldStrength = shield;
    }

    /**
     * Decrements the starship's shield strength. This should be used
     * when the starship gets hit during the game.
     * */
    public void decreaseShieldStrength() {
        this.shieldStrength--;
    }

    /**
     * Resets the starship's current shield strength back to its
     * default (max value provided during instantiation).
     * */
    public void repairShieldStrength() {
        this.shieldStrength = this.maxShieldStrength;
    }

    /**
     * Provides the default number of shots provided during instantiation.
     *
     * @return Maximum shots.
     * */
    public int getMaxShots() {
        return this.maxShots;
    }

    /**
     * Provides the starship's current number of shots left.
     *
     * @return Number of shots.
     * */
    public int getShots() {
        return this.shots;
    }

    /**
     * @param shots The new shots value
     * */
    public void setShots(int shots) {
        this.shots = shots;
    }

    /**
     * Decrements the starship's number of shots. This should be used
     * when the starship executes the 'fire' command.
     * */
    public void decreaseShots() {
        this.shots--;
    }

    /**
     * Resets the starship's number of shots back to its
     * default (max value provided during instantiation).
     * */
    public void rearmShots() {
        this.shots = this.maxShots;
    }

    /**
     * Provides the number of starships this starship has destroyed.
     * @return The current kill count.
     */
    public int getKillCount() {
        return this.killCount;
    }

    /**
     * Increments the starship's kill count by one. This should be used
     * when a starship this starship fired at is destroyed.
     */
    public void incrementKillCount() {
        this.killCount++;
    }

    /**
     * Checks whether the starship's bullet can hit another (target) starship positioned ahead of it.
     *
     * @param target The starship you wish to 'fire' at.
     * @return True -> If the 'target' starship is within the bullet's range, and will be hit.<br>
     *         False -> If the 'target' starship is out of range.
     * */
    public boolean canBulletHit(Starship target){

        switch ( getHeading() ){
            case NORTH:
                if( target.getPosition().getX() == this.getPosition().getX() )
                    if( target.getPosition().getY() >  this.getPosition().getY() )
                        return true;
                break;

            case EAST:
                if( target.getPosition().getX() > this.getPosition().getX() )
                    if( target.getPosition().getY() ==  this.getPosition().getY() )
                        return true;
                break;

            case SOUTH:
                if( target.getPosition().getX() == this.getPosition().getX() )
                    if( target.getPosition().getY() <  this.getPosition().getY() )
                        return true;
                break;

            case WEST:
                if( target.getPosition().getX() < this.getPosition().getX() )
                    if( target.getPosition().getY() ==  this.getPosition().getY() )
                        return true;
                break;
        }

        return false;   // if the target starship is not in range
    }

    @Override
    public String toString() {
        return "[" + this.position.getX() + "," + this.position.getY() + "] "
                + this.callsign + "> " + this.status + " [kills: " + this.killCount + "]";
    }


    public void setPosition(Position nextPos) {
        this.position = nextPos;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }
}
