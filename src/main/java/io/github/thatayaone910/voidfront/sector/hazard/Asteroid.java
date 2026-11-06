package io.github.thatayaone910.voidfront.sector.hazard;

import io.github.thatayaone910.voidfront.sector.Position;

/**
 * Represents an asteroid hazard in the sector.
 * Asteroids are static hazards that occupy a single coordinate point
 * and prevent starships from moving into that specific space.
 */
public class Asteroid extends SpaceHazard {

    /**
     * Constructs a new Asteroid at the specified coordinates.
     * The hazard type is automatically set to "Asteroid".
     *
     * @param x the horizontal coordinate.
     * @param y the vertical coordinate.
     */
    public Asteroid(int x, int y) {
        super(x, y);
        this.type = "Asteroid";
    }

    /**
     * Determines whether the asteroid occupies the given position.
     *
     * @param pos The position to check for a collision.
     * @return true if the asteroid occupies the specified position; false otherwise.
     */
    @Override
    public boolean blocksPosition(Position pos) {
        return pos.getX() == this.position.getX() && pos.getY() == this.position.getY();
    }
}