package io.github.thatayaone910.voidfront.sector.hazard;

import io.github.thatayaone910.voidfront.sector.Position;

/**
 * Represents a VoidRift hazard within the Starship Sector.
 * Void rifts are lethal hazards. A starship entering one is destroyed.
 */
public class VoidRift extends SpaceHazard {

    /**
     * Constructs a new VoidRift at the specified coordinates.
     * The hazard type is automatically set to "VoidRift".
     *
     * @param x the horizontal coordinate.
     * @param y the vertical coordinate.
     */
    public VoidRift(int x, int y) {
        super(x, y);
        this.type = "VoidRift";
    }

    /**
     * Determines whether the rift occupies the given position.
     *
     * @param pos The position to check for a collision.
     * @return true if the void rift occupies the specified position; false otherwise.
     */
    @Override
    public boolean blocksPosition(Position pos) {
        return pos.getX() == this.position.getX() && pos.getY() == this.position.getY();
    }
}