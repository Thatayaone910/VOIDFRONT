package io.github.thatayaone910.voidfront.sector.hazard;

import io.github.thatayaone910.voidfront.sector.Position;

/**
 * Represents a Nebula hazard within the Starship Sector.
 * Nebulas are space hazards that starships cannot enter.
 * While currently implemented as a single-point hazard, the logic
 * allows for future expansion into larger surface areas.
 */
public class Nebula extends SpaceHazard {

    /**
     * Constructs a new Nebula at the specified coordinates.
     * The hazard type is automatically set to "Nebula".
     *
     * @param x the horizontal coordinate.
     * @param y the vertical coordinate.
     */
    public Nebula(int x, int y) {
        super(x, y);
        this.type = "Nebula";
    }

    /**
     * Determines whether the nebula occupies the given position.
     *
     * @param pos The position to check for a collision.
     * @return true if the nebula occupies the specified position; false otherwise.
     */
    @Override
    public boolean blocksPosition(Position pos) {
        // The current implementation occupies a single coordinate.
        return pos.getX() == this.position.getX() && pos.getY() == this.position.getY();
    }
}