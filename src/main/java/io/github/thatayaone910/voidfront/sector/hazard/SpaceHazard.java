package io.github.thatayaone910.voidfront.sector.hazard;

import io.github.thatayaone910.voidfront.sector.Position;

/**
 * Represents a generic physical hazard within the Starship Sector.
 * This is an abstract base class that defines the common properties of all hazards,
 * such as their position and type. Specific hazard behaviors (like how they block
 * movement) are implemented by subclasses.
 */
public abstract class SpaceHazard {

    /** The coordinate location of the hazard in the sector. */
    protected Position position;

    /** The specific category of the hazard (e.g., "Nebula", "Asteroid"). */
    protected String type;

    /**
     * Constructs a new SpaceHazard at the specified coordinates.
     *
     * @param x the horizontal coordinate.
     * @param y the vertical coordinate.
     */
    public SpaceHazard(int x, int y) {
        this.position = new Position(x, y);
    }

    /**
     * Gets the current position of the hazard.
     * @return the {@link Position} object representing where this hazard is located.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets the descriptive type of the hazard.
     * @return a string representing the hazard type.
     */
    public String getType() {
        return type;
    }

    /**
     * Determines if a specific coordinate is blocked by this hazard.
     * This method must be implemented by subclasses to define the specific
     * area or "hitbox" that the hazard occupies.
     *
     * @param pos The position to check for a collision.
     * @return true if the position is occupied/blocked by this hazard; false otherwise.
     */
    public abstract boolean blocksPosition(Position pos);
}