package io.github.thatayaone910.voidfront.sector;

/**
 * Represents a 2D coordinate point within the Starship Sector.
 * This class is used to track locations of starships and hazards, and to
 * perform boundary checks.
 */
public class Position {

    private final int x;
    private final int y;

    /**
     * Constructs a new Position with the specified coordinates.
     *
     * @param x the horizontal coordinate.
     * @param y the vertical coordinate.
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the horizontal coordinate.
     * @return the x-coordinate as an integer.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the vertical coordinate.
     * @return the y-coordinate as an integer.
     */
    public int getY() {
        return y;
    }

    /**
     * Compares this Position to another object to determine equality.
     * Two Position objects are considered equal if they share the same x and y values.
     *
     * @param o the object to compare this Position against.
     * @return true if the object is a Position with identical coordinates; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Position position = (Position) o;

        if (x != position.x) {
            return false;
        }
        return y == position.y;
    }

    /**
     * Determines if this position falls within a rectangular area defined by
     * two boundary positions.
     *
     * @param topLeft the position representing the top-left corner of the boundary.
     * @param bottomRight the position representing the bottom-right corner of the boundary.
     * @return true if this position is inside or on the edge of the specified bounds;
     *         false otherwise.
     */
    public boolean isIn(Position topLeft, Position bottomRight) {
        int leftSide = Math.min(topLeft.getX(), bottomRight.getX());
        int rightSide = Math.max(topLeft.getX(), bottomRight.getX());
        int bottomSide = Math.min(topLeft.getY(), bottomRight.getY());
        int topSide = Math.max(topLeft.getY(),bottomRight.getY());

        boolean withinLeftRight = this.x >= leftSide && this.x <= rightSide;
        boolean withinTopBottom = this.y >= bottomSide && this.y <= topSide;

        return withinLeftRight && withinTopBottom;
    }

    /**Gives a clean representation of the position.*/
    @Override
    public String toString(){
        return "["+getX()+","+getY()+"]";
    }
}