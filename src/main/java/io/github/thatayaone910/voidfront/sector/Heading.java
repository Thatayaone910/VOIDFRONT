package io.github.thatayaone910.voidfront.sector;

public enum Heading {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public int xOffset() {
        return switch (this) {
            case EAST -> 1;
            case WEST -> -1;
            case NORTH, SOUTH -> 0;
        };
    }

    public int yOffset() {
        return switch (this) {
            case NORTH -> 1;
            case SOUTH -> -1;
            case EAST, WEST -> 0;
        };
    }
}
